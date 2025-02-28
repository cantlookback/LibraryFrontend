package com.libraryfront.rcp.parts.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.libraryfront.rcp.entity.Book;

public class ChangeBookDialog extends Dialog {
    private Text nameText;
    private Text authorText;
    private Text publisherText;
    private Spinner yearSpinner;

    private Book book; // Исходный объект
    private Book updatedBook; // Изменённый объект

    public ChangeBookDialog(Shell parentShell, Book book) {
        super(parentShell);
        this.book = book;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(2, false));

        // Название книги
        Label nameLabel = new Label(container, SWT.NONE);
        nameLabel.setText("Название:");
        nameText = new Text(container, SWT.BORDER);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        nameText.setText(book.getName());

        // Автор
        Label authorLabel = new Label(container, SWT.NONE);
        authorLabel.setText("Автор:");
        authorText = new Text(container, SWT.BORDER);
        authorText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        authorText.setText(book.getAuthor());

        // Издательство
        Label publisherLabel = new Label(container, SWT.NONE);
        publisherLabel.setText("Издательство:");
        publisherText = new Text(container, SWT.BORDER);
        publisherText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        publisherText.setText(book.getPublisher());

        // Год издания
        Label yearLabel = new Label(container, SWT.NONE);
        yearLabel.setText("Год издания:");
        yearSpinner = new Spinner(container, SWT.BORDER);
        yearSpinner.setMinimum(1000);
        yearSpinner.setMaximum(3000);
        yearSpinner.setSelection(book.getYear());

        return container;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "Сохранить", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "Отмена", false);
    }

    @Override
    protected void okPressed() {
        String updatedName = nameText.getText();
        String updatedAuthor = authorText.getText();
        String updatedPublisher = publisherText.getText();
        int updatedYear = yearSpinner.getSelection();

        updatedBook = new Book(book.getId(), updatedName, updatedAuthor, updatedPublisher, updatedYear, book.isAvailable());

        super.okPressed();
    }

    public Book getUpdatedBook() {
        return updatedBook;
    }
}