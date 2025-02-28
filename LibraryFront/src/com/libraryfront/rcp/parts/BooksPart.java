package com.libraryfront.rcp.parts;

import com.libraryfront.rcp.entity.Book;
import com.libraryfront.rcp.parts.dialog.AddBookDialog;
import com.libraryfront.rcp.parts.dialog.ChangeBookDialog;
import com.libraryfront.rcp.util.JsonParser;
import com.libraryfront.rcp.api.ApiClient;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BooksPart {

	private Table table;
    private List<Book> books = new ArrayList<>();

    @Inject
    private EPartService partService;
    
    @PreDestroy
    public void onClose(MPart part) {
        partService.hidePart(part); // Вместо удаления скрываем часть
    }
    
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
        String[] columns = {"Название", "Автор", "Издатель", "Год", "Доступна"};
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
            	
                AddBookDialog dialog = new AddBookDialog(shell);
                if (dialog.open() == Window.OK) {
                    Book newBook = dialog.getNewBook();

                    try {
                        // Отправляем книгу на сервер
                        ApiClient.post("/books", JsonParser.toJson(newBook));

                        // Обновляем таблицу
                        loadBooks();
                        refreshTable();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        // Показываем сообщение об ошибке
                        MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                        messageBox.setText("Ошибка");
                        messageBox.setMessage("Не удалось добавить книгу: " + ex.getMessage());
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
				ChangeBookDialog dialog = new ChangeBookDialog(changeButton.getShell(),
						books.get(table.getSelectionIndex()));
				if (dialog.open() == Window.OK) {
					Book updatedBook = dialog.getUpdatedBook();
					if (updatedBook != null) {
						// Отправляем обновлённые данные на сервер
						try {
							ApiClient.put("/books", JsonParser.toJson(updatedBook));
							loadBooks();
							refreshTable();
						} catch (IOException ex) {
							ex.printStackTrace();
							// Показываем сообщение об ошибке
							MessageBox messageBox = new MessageBox(changeButton.getShell(), SWT.ICON_ERROR | SWT.OK);
							messageBox.setText("Ошибка");
							messageBox.setMessage("Не удалось обновить книгу: " + ex.getMessage());
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
                Book selectedBook = (Book) selectedItem.getData();
                if (showConfirmationDialog(parent.getShell(), "Удаление книги", 
                                           "Вы уверены, что хотите удалить книгу: " + selectedBook.getName() + "?")) {
                    deleteBook(selectedBook.getId());
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
	
	private void deleteBook(Long id) {
		try {
            // Отправляем книгу на сервер
            ApiClient.delete("/books", id);

            loadBooks();
            refreshTable();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}

	// Загрузка данных с сервера
    private void loadBooks() {
        try {
            String response = ApiClient.get("/books"); // Используем статический метод
            this.books = JsonParser.parseBooks(response); // Парсим JSON
            refreshTable();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
	// Обновление таблицы
    private void refreshTable() {
        table.removeAll();
        for (Book book : books) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[]{
                    book.getName(),
                    book.getAuthor(),
                    book.getPublisher(),
                    String.valueOf(book.getYear()),
                    book.isAvailable() ? "Yes" : "No"
            });
            item.setData(book);
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
		this.loadBooks();
		this.refreshTable();
	}
}