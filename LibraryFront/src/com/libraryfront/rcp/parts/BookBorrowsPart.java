package com.libraryfront.rcp.parts;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
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
import com.libraryfront.rcp.entity.Book;
import com.libraryfront.rcp.entity.BookBorrow;
import com.libraryfront.rcp.parts.dialog.AddBookBorrowDialog;
import com.libraryfront.rcp.parts.dialog.ChangeBookDialog;
import com.libraryfront.rcp.parts.dialog.CreateReportDialog;
import com.libraryfront.rcp.util.JsonParser;

public class BookBorrowsPart {

	private Table table;
	private List<BookBorrow> book_borrows = new ArrayList<>();

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
		String[] columns = { "Гость", "Книга", "Дата", "Статус" };
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

				AddBookBorrowDialog dialog = new AddBookBorrowDialog(shell);
				if (dialog.open() == Window.OK) {
					BookBorrow newBookBorrow = dialog.getNewBookBorrow();
					Book borrowedBook = dialog.getSelectedBook();
					try {
						// Отправляем книгу на сервер
						ApiClient.post("/bookBorrows", JsonParser.toJson(newBookBorrow));
						if (borrowedBook.isAvailable()) {
							borrowedBook.setAvailable(false);
							ApiClient.put("/books", JsonParser.toJson(borrowedBook));
						} else {
							borrowedBook.setAvailable(true);
							ApiClient.put("/books", JsonParser.toJson(borrowedBook));
						}
						// Обновляем таблицу
						loadBookBorrows();
						refreshTable();
					} catch (IOException ex) {
						ex.printStackTrace();
						// Показываем сообщение об ошибке
						MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
						messageBox.setText("Ошибка");
						messageBox.setMessage("Не удалось добавить запись: " + ex.getMessage());
						messageBox.open();
					}
				}
			}
		});

		Button createReportButton = new Button(buttonComposite, SWT.PUSH);
		createReportButton.setText("Отчёт");
		createReportButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		createReportButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CreateReportDialog dialog = new CreateReportDialog(createReportButton.getShell());
				dialog.open();
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
				BookBorrow selectedBookBorrow = (BookBorrow) selectedItem.getData();
				if (showConfirmationDialog(parent.getShell(), "Удаление книги",
						"Вы уверены, что хотите удалить запись?")) {
					deleteBookBorrow(selectedBookBorrow.getId());
					refreshTable();
				}
			}
		});

		this.loadBookBorrows();
		this.refreshTable();
	}

	private void deleteBookBorrow(Long id) {
		try {
			ApiClient.delete("/bookBorrows", id);
			loadBookBorrows();
			refreshTable();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	private boolean showConfirmationDialog(Shell shell, String title, String message) {
		MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		dialog.setText(title);
		dialog.setMessage(message);
		return dialog.open() == SWT.YES;
	}

	// Загрузка данных с сервера
	private void loadBookBorrows() {
		try {
			String response = ApiClient.get("/bookBorrows"); // Используем статический метод
			this.book_borrows = JsonParser.parseBookBorrows(response); // Парсим JSON
			refreshTable();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Обновление таблицы
	private void refreshTable() {
		table.removeAll();
		for (BookBorrow book_borrow : book_borrows) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(new String[] { String.valueOf(book_borrow.getPerson().getName()),
					String.valueOf(book_borrow.getBook().getName()),
					book_borrow.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")),
					book_borrow.isBorrowed() ? "Выдача" : "Приём" });
			item.setData(book_borrow);
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
		this.loadBookBorrows();
		this.refreshTable();
	}
}