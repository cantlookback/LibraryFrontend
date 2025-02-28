package com.libraryfront.rcp.parts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.libraryfront.rcp.api.ApiClient;
import com.libraryfront.rcp.entity.Person;
import com.libraryfront.rcp.parts.dialog.AddPersonDialog;
import com.libraryfront.rcp.parts.dialog.ChangePersonDialog;
import com.libraryfront.rcp.util.JsonParser;

public class PersonsPart {

	private Table table;
	private List<Person> persons = new ArrayList<>();

	@Inject
	private MPart part;

	@PostConstruct
	public void createComposite(Composite parent) {
		// Устанавливаем GridLayout для родительского Composite
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		// Создаём таблицу
		table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// Настраиваем GridData для таблицы
		GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(tableGridData);

		// Создаём колонки
		String[] columns = { "Имя", "Пол", "Возраст" };
		for (String column : columns) {
			TableColumn tableColumn = new TableColumn(table, SWT.NONE);
			tableColumn.setText(column);
			tableColumn.setWidth(100);
		}

		// Создаём контейнер для кнопок
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		GridLayout buttonLayout = new GridLayout(3, true); // 3 кнопки в ряд, равномерно
		buttonLayout.marginWidth = 10; // Отступы по краям
		buttonLayout.marginHeight = 10;
		buttonLayout.horizontalSpacing = 10; // Расстояние между кнопками
		buttonComposite.setLayout(buttonLayout);

		// Устанавливаем GridData, чтобы контейнер занимал всю ширину
		GridData buttonGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		buttonComposite.setLayoutData(buttonGridData);

		// Кнопка "Добавить"
		Button addButton = new Button(buttonComposite, SWT.PUSH);
		addButton.setText("Добавить");
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Открываем диалог для добавления книги
				Shell shell = addButton.getShell();

				AddPersonDialog dialog = new AddPersonDialog(shell);
				if (dialog.open() == Window.OK) {
					Person newPerson = dialog.getNewPerson();

					try {
						// Отправляем книгу на сервер
						ApiClient.post("/persons", JsonParser.toJson(newPerson));

						// Обновляем таблицу
						loadPersons();
						refreshTable();
					} catch (IOException ex) {
						ex.printStackTrace();
						// Показываем сообщение об ошибке
						MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
						messageBox.setText("Ошибка");
						messageBox.setMessage("Не удалось добавить гостя: " + ex.getMessage());
						messageBox.open();
					}
				}
			}
		});

		// Кнопка "Изменить"
		Button changeButton = new Button(buttonComposite, SWT.PUSH);
		changeButton.setText("Изменить");
		changeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		changeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ChangePersonDialog dialog = new ChangePersonDialog(changeButton.getShell(),
						persons.get(table.getSelectionIndex()));
				if (dialog.open() == Window.OK) {
					Person updatedPerson = dialog.getUpdatedPerson();
					if (updatedPerson != null) {
						// Отправляем обновлённые данные на сервер
						try {
							ApiClient.put("/persons", JsonParser.toJson(updatedPerson));
							loadPersons();
							refreshTable();
						} catch (IOException ex) {
							ex.printStackTrace();
							// Показываем сообщение об ошибке
							MessageBox messageBox = new MessageBox(changeButton.getShell(), SWT.ICON_ERROR | SWT.OK);
							messageBox.setText("Ошибка");
							messageBox.setMessage("Не удалось обновить гостя: " + ex.getMessage());
							messageBox.open();
						}
					}
				}
			}
		});

		// Кнопка "Удалить"
		Button deleteButton = new Button(buttonComposite, SWT.PUSH);
		deleteButton.setText("Удалить");
		deleteButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		deleteButton.addListener(SWT.Selection, event -> {
			int index = table.getSelectionIndex();
			if (index != -1) {
				TableItem selectedItem = table.getItem(index);
				Person selectedPerson = (Person) selectedItem.getData();
				if (showConfirmationDialog(parent.getShell(), "Удаление книги",
						"Вы уверены, что хотите удалить гостя: " + selectedPerson.getName() + "?")) {
					deletePerson(selectedPerson.getId());
					refreshTable();
				}
			}
		});

	}

	private boolean showConfirmationDialog(Shell shell, String title, String message) {
		MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		dialog.setText(title);
		dialog.setMessage(message);
		return dialog.open() == SWT.YES;
	}

	// Загрузка данных с сервера
	private void loadPersons() {
		try {
			String response = ApiClient.get("/persons"); // Используем статический метод
			this.persons = JsonParser.parsePersons(response); // Парсим JSON
			refreshTable();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void deletePerson(Long id) {
		try {
			// Отправляем книгу на сервер
			ApiClient.delete("/persons", id);

			loadPersons();
			refreshTable();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	// Обновление таблицы
	private void refreshTable() {
		table.removeAll();
		for (Person person : persons) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(new String[] { person.getName(), person.getSex(), String.valueOf(person.getAge()) });
			item.setData(person);
		}
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).pack();
		}
		table.redraw();
		table.update();
		table.getParent().layout(true, true);
	}

	@Focus
	public void setFocus() {
		this.loadPersons();
		this.refreshTable();
	}
}