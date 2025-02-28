package com.libraryfront.rcp.parts.dialog;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import com.libraryfront.rcp.api.ApiClient;
import com.libraryfront.rcp.entity.Book;
import com.libraryfront.rcp.entity.Person;
import com.libraryfront.rcp.util.JsonParser;
import com.libraryfront.rcp.entity.BookBorrow;

public class AddBookBorrowDialog extends Dialog {

	private Combo personCombo;
	private Combo bookCombo;
	private Button borrowedYesButton;
	private Button borrowedNoButton;
	private DateTime datePicker;
	private DateTime timePicker;
	private Button nowCheckbox;

	private List<Person> persons;
	private List<Book> books;

	// Результирующие значения
	private Person selectedPerson;
	private Book selectedBook;
	private boolean isBorrowed;
	private LocalDateTime selectedDateTime;

	private BookBorrow newBookBorrow;

	public AddBookBorrowDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Добавить запись");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		// Используем сетку с 2 столбцами
		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);

		getPersons();

		// 1. Радиогруппа для режима
		Label modeLabel = new Label(container, SWT.NONE);
		modeLabel.setText("Режим:");
		Composite modeComposite = new Composite(container, SWT.NONE);
		modeComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		borrowedYesButton = new Button(modeComposite, SWT.RADIO);
		borrowedYesButton.setText("Выдача");
		borrowedNoButton = new Button(modeComposite, SWT.RADIO);
		borrowedNoButton.setText("Приём");
		// По умолчанию выбираем "Выдача"
		borrowedYesButton.setSelection(true);
		borrowedNoButton.setSelection(false);

		getBooks(true);

		// 2. Выбор Person (Combo)
		Label personLabel = new Label(container, SWT.NONE);
		personLabel.setText("Гость:");
		personCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		for (Person p : persons) {
			personCombo.add(p.getName());
		}
		if (!persons.isEmpty()) {
			personCombo.select(0);
		}

		// 3. Выбор книги (Combo)
		Label bookLabel = new Label(container, SWT.NONE);
		bookLabel.setText("Книга:");
		bookCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		// Заполним список книг в зависимости от режима (по умолчанию "Выдача")
		updateBookCombo();

		// Добавляем слушатели изменения режима и выбора пользователя
		borrowedYesButton.addListener(SWT.Selection, e -> updateBookCombo());
		borrowedNoButton.addListener(SWT.Selection, e -> updateBookCombo());
		personCombo.addListener(SWT.Selection, e -> {
			if (borrowedNoButton.getSelection()) {
				updateBookCombo();
			}
		});

		// 4. Выбор даты и времени + чекбокс "Now"
		Label dateTimeLabel = new Label(container, SWT.NONE);
		dateTimeLabel.setText("Дата и время:");
		Composite dateTimeComposite = new Composite(container, SWT.NONE);
		// Используем GridLayout с 3 столбцами: дата, время, чекбокс
		GridLayout dtLayout = new GridLayout(3, false);
		dateTimeComposite.setLayout(dtLayout);
		datePicker = new DateTime(dateTimeComposite, SWT.BORDER | SWT.DROP_DOWN | SWT.DATE);
		timePicker = new DateTime(dateTimeComposite, SWT.BORDER | SWT.TIME);
		nowCheckbox = new Button(dateTimeComposite, SWT.CHECK);
		nowCheckbox.setText("Сейчас");
		nowCheckbox.addListener(SWT.Selection, e -> {
			boolean nowSelected = nowCheckbox.getSelection();
			datePicker.setEnabled(!nowSelected);
			timePicker.setEnabled(!nowSelected);
		});

		return container;
	}

	private void updateBookCombo() {
		bookCombo.removeAll();
		if (borrowedYesButton.getSelection()) {
			// Режим "Выдача": только доступные книги
			for (Book book : books) {
				if (book.isAvailable()) {
					int index = bookCombo.getItemCount();
					bookCombo.add(book.getName()); // Добавляем название книги
					bookCombo.setData(String.valueOf(index), book.getId()); // Сохраняем ID книги по индексу
				}
			}
		} else {
			// Режим "Приём": только книги, которые взял выбранный пользователь
			int personIndex = personCombo.getSelectionIndex();
			if (personIndex >= 0) {
				Person person = persons.get(personIndex);
				List<Book> borrowedBooks = getBorrowedBooksForPerson(person);
				for (Book book : borrowedBooks) {
					int index = bookCombo.getItemCount();
					bookCombo.add(book.getName()); // Добавляем название книги
					bookCombo.setData(String.valueOf(index), book.getId()); // Сохраняем ID книги по индексу
				}
			}
		}
		// Если список не пуст, выбираем первый элемент по умолчанию
		if (bookCombo.getItemCount() > 0) {
			bookCombo.select(0);
		}
	}

	private List<Book> getBorrowedBooksForPerson(Person person) {
		List<BookBorrow> allBorrows = this.getBookBorrows();

		// Сначала группируем все записи по ID книги и оставляем только самую последнюю
		// операцию
		Map<Long, BookBorrow> latestBorrows = allBorrows.stream()
				.collect(Collectors.toMap(borrow -> borrow.getBook().getId(), // Группируем по ID книги
						borrow -> borrow, // Значение — сама запись
						(b1, b2) -> b1.getDate().isAfter(b2.getDate()) ? b1 : b2 // Берём запись с самой поздней датой
				));

		// Затем фильтруем книги, которые у данного пользователя и не доступны
		// (is_available = false)
		return latestBorrows.values().stream().filter(borrow -> borrow.getPerson().getId().equals(person.getId())) // Только
																													// для
																													// текущего
																													// пользователя
				.filter(borrow -> !borrow.getBook().isAvailable()) // Только книги, которые ещё не сданы
				.map(BookBorrow::getBook).collect(Collectors.toList());
	}

	private List<BookBorrow> getBookBorrows() {
		try {
			String response = ApiClient.get("/bookBorrows"); // Используем статический метод
			return JsonParser.parseBookBorrows(response); // Парсим JSON
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void getPersons() {
		try {
			String response = ApiClient.get("/persons"); // Используем статический метод
			this.persons = JsonParser.parsePersons(response); // Парсим JSON
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void getBooks(boolean is_borrow) {
		try {
			String response = ApiClient.get("/books"); // Используем статический метод
			this.books = JsonParser.parseBooks(response); // Парсим JSON
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Добавить", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "Отмена", false);
	}

	@Override
	protected void okPressed() {
		int personIndex = personCombo.getSelectionIndex();
		if (personIndex >= 0) {
			selectedPerson = persons.get(personIndex);
		}

		int bookIndex = bookCombo.getSelectionIndex();
		if (bookIndex >= 0) {
			// Получаем ID книги из Combo
			Long bookId = (Long) bookCombo.getData(String.valueOf(bookIndex));

			// Находим книгу в списке books по ID
			selectedBook = books.stream().filter(book -> book.getId().equals(bookId)).findFirst().orElse(null);
		}

		if (selectedBook == null) {
			MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Ошибка");
			messageBox.setMessage("Ошибка выбора книги. Повторите попытку.");
			messageBox.open();
			return;
		}

		isBorrowed = borrowedYesButton.getSelection();

		if (nowCheckbox.getSelection()) {
			selectedDateTime = LocalDateTime.now();
		} else {
			int year = datePicker.getYear();
			int month = datePicker.getMonth() + 1;
			int day = datePicker.getDay();
			int hour = timePicker.getHours();
			int minute = timePicker.getMinutes();
			int second = timePicker.getSeconds();
			selectedDateTime = LocalDateTime.of(year, month, day, hour, minute, second, 1);
		}

		this.newBookBorrow = new BookBorrow(selectedPerson, selectedBook, isBorrowed, selectedDateTime);

		super.okPressed();
	}

	public Person getSelectedPerson() {
		return selectedPerson;
	}

	public Book getSelectedBook() {
		return selectedBook;
	}

	public boolean isBorrowed() {
		return isBorrowed;
	}

	public LocalDateTime getSelectedDate() {
		return selectedDateTime;
	}

	public BookBorrow getNewBookBorrow() {
		return newBookBorrow;
	}
}