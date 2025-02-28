package com.libraryfront.rcp.parts.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.libraryfront.rcp.entity.Book;

public class AddBookDialog extends Dialog {

    private Text nameText;
    private Text authorText;
    private Text publisherText;
    private Text yearText;
    private Button availableButton;

    private Book newBook;

    public AddBookDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Добавить книгу");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);

        // Название
        Label nameLabel = new Label(container, SWT.NONE);
        nameLabel.setText("Название:");
        nameText = new Text(container, SWT.BORDER);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Автор
        Label authorLabel = new Label(container, SWT.NONE);
        authorLabel.setText("Автор:");
        authorText = new Text(container, SWT.BORDER);
        authorText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Издательство
        Label publisherLabel = new Label(container, SWT.NONE);
        publisherLabel.setText("Издательство:");
        publisherText = new Text(container, SWT.BORDER);
        publisherText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Год
        Label yearLabel = new Label(container, SWT.NONE);
        yearLabel.setText("Год:");
        yearText = new Text(container, SWT.BORDER);
        yearText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Доступность
        Label availableLabel = new Label(container, SWT.NONE);
        availableLabel.setText("Доступна:");
        availableButton = new Button(container, SWT.CHECK);
        availableButton.setSelection(true); // По умолчанию книга доступна

        return container;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "Добавить", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "Отмена", false);
    }

    @Override
    protected void okPressed() {
        // Получаем данные из полей
        String name = nameText.getText();
        String author = authorText.getText();
        String publisher = publisherText.getText();
        int year = Integer.parseInt(yearText.getText());
        boolean available = availableButton.getSelection();

        // Создаём новую книгу
        newBook = new Book(name, author, publisher, year, available);

        super.okPressed();
    }

    public Book getNewBook() {
        return newBook;
    }
}