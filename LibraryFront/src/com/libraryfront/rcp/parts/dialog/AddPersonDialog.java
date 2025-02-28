package com.libraryfront.rcp.parts.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import com.libraryfront.rcp.entity.Person;

public class AddPersonDialog extends Dialog {

    private Text nameText;
    private Button maleButton;
    private Button femaleButton;

    private Text ageText;
    
    private Person newPerson;

    public AddPersonDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Добавить гостя");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);

        // ФИО
        Label nameLabel = new Label(container, SWT.NONE);
        nameLabel.setText("ФИО:");
        nameText = new Text(container, SWT.BORDER);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Пол
        Label sexLabel = new Label(container, SWT.NONE);
        sexLabel.setText("Пол:");
        Composite sexComposite = new Composite(container, SWT.NONE);
        sexComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
        
        maleButton = new Button(sexComposite, SWT.RADIO);
        maleButton.setText("М");
        femaleButton = new Button(sexComposite, SWT.RADIO);
        femaleButton.setText("Ж");
        maleButton.setSelection(true);

        // Возраст
        Label yearLabel = new Label(container, SWT.NONE);
        yearLabel.setText("Возраст:");
        ageText = new Text(container, SWT.BORDER);
        ageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

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
        boolean sex = maleButton.getSelection();
        int age = Integer.parseInt(ageText.getText());

        // Создаём новую книгу
        newPerson = new Person(name, sex, age);

        super.okPressed();
    }

    public Person getNewPerson() {
        return newPerson;
    }
}