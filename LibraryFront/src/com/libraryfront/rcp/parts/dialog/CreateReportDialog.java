package com.libraryfront.rcp.parts.dialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import com.libraryfront.rcp.api.ApiClient;
import com.libraryfront.rcp.entity.BookBorrow;
import com.libraryfront.rcp.entity.Person;
import com.libraryfront.rcp.util.JsonParser;

public class CreateReportDialog extends Dialog {

    private Combo personCombo;
    private DateTime startDatePicker;
    private DateTime endDatePicker;
    private Button generateButton;

    private List<Person> persons;
    private List<BookBorrow> bookBorrows;

    public CreateReportDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Создать отчет");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(2, false));

        // Загружаем данные
        loadPersons();
        loadBookBorrows();

        // 1. Выбор пользователя
        Label personLabel = new Label(container, SWT.NONE);
        personLabel.setText("Пользователь:");
        personCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        for (Person p : persons) {
            personCombo.add(p.getName());
        }
        if (!persons.isEmpty()) {
            personCombo.select(0);
        }

        // 2. Выбор диапазона дат
        Label dateLabel = new Label(container, SWT.NONE);
        dateLabel.setText("Выберите диапазон дат:");
        Composite dateComposite = new Composite(container, SWT.NONE);
        dateComposite.setLayout(new RowLayout(SWT.HORIZONTAL));

        startDatePicker = new DateTime(dateComposite, SWT.BORDER | SWT.DATE);
        new Label(dateComposite, SWT.NONE).setText("—");
        endDatePicker = new DateTime(dateComposite, SWT.BORDER | SWT.DATE);

        return container;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        generateButton = createButton(parent, IDialogConstants.OK_ID, "Сформировать отчет", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "Отмена", false);
    }

    @Override
    protected void okPressed() {
        int personIndex = personCombo.getSelectionIndex();
        if (personIndex >= 0) {
            Person selectedPerson = persons.get(personIndex);

            // Получаем даты
            LocalDate startDate = LocalDate.of(startDatePicker.getYear(), startDatePicker.getMonth() + 1,
                    startDatePicker.getDay());
            LocalDate endDate = LocalDate.of(endDatePicker.getYear(), endDatePicker.getMonth() + 1,
                    endDatePicker.getDay());

            List<BookBorrow> filteredBorrows = filterBookBorrows(selectedPerson, startDate, endDate);

            if (filteredBorrows.isEmpty()) {
                showMessage("Нет данных", "Нет выданных книг за указанный период.");
            } else {
                generateCsvReport(filteredBorrows, selectedPerson, startDate, endDate);
            }
        }
        super.okPressed();
    }

    private void loadPersons() {
        try {
            String response = ApiClient.get("/persons");
            persons = JsonParser.parsePersons(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBookBorrows() {
        try {
            String response = ApiClient.get("/bookBorrows");
            bookBorrows = JsonParser.parseBookBorrows(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<BookBorrow> filterBookBorrows(Person person, LocalDate startDate, LocalDate endDate) {
        return bookBorrows.stream()
                .filter(borrow -> borrow.getPerson().getId().equals(person.getId()))
                .filter(borrow -> {
                    LocalDate borrowDate = borrow.getDate().toLocalDate();
                    return !borrowDate.isBefore(startDate) && !borrowDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
    }

    private void generateCsvReport(List<BookBorrow> borrows, Person person, LocalDate startDate, LocalDate endDate) {
        File file = new File("report_" + person.getId() + "_" + startDate + "_" + endDate + ".csv");

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            // Добавляем BOM, чтобы Excel корректно распознал UTF-8
            writer.write("\uFEFF");

            // Заголовок
            writer.append("Отчет о выданных книгах\n");
            writer.append("Пользователь: ").append(person.getName()).append("\n");
            writer.append("Период: ").append(startDate.toString()).append(" — ").append(endDate.toString()).append("\n\n");

            // Заголовки колонок
            writer.append("Название книги;Автор;Дата выдачи;Выдана/Принята\n");

            // Данные
            for (BookBorrow borrow : borrows) {
                writer.append(borrow.getBook().getName()).append(";");
                writer.append(borrow.getBook().getAuthor()).append(";");
                writer.append(borrow.getDate().toString()).append(";");
                writer.append(borrow.isBorrowed() ? "Выдана" : "Принята").append("\n");
            }

            writer.flush();
            showMessage("Отчет сформирован", "Файл сохранен: " + file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Ошибка", "Ошибка при создании отчета.");
        }
    }

    private void showMessage(String title, String message) {
        MessageBox dialog = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
        dialog.setText(title);
        dialog.setMessage(message);
        dialog.open();
    }
}
