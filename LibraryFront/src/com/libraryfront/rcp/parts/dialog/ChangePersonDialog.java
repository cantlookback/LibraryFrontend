package com.libraryfront.rcp.parts.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.libraryfront.rcp.entity.Person;

public class ChangePersonDialog extends Dialog {
    private Text nameText;
    private Button maleButton;
    private Button femaleButton;
    private Spinner ageSpinner;

    private Person person; // Исходный объект
    private Person updatedPerson; // Изменённый объект

    public ChangePersonDialog(Shell parentShell, Person person) {
        super(parentShell);
        this.person = person;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(2, false));

        // ФИО
        Label nameLabel = new Label(container, SWT.NONE);
        nameLabel.setText("ФИО:");
        nameText = new Text(container, SWT.BORDER);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        nameText.setText(person.getName());

        // Пол (радиокнопки)
        Label genderLabel = new Label(container, SWT.NONE);
        genderLabel.setText("Пол:");
        Composite genderComposite = new Composite(container, SWT.NONE);
        genderComposite.setLayout(new GridLayout(2, false));
        maleButton = new Button(genderComposite, SWT.RADIO);
        maleButton.setText("Мужской");
        femaleButton = new Button(genderComposite, SWT.RADIO);
        femaleButton.setText("Женский");

        if (person.getSex() == "Male") {
            maleButton.setSelection(true);
        } else {
            femaleButton.setSelection(true);
        }

        // Возраст (Spinner)
        Label ageLabel = new Label(container, SWT.NONE);
        ageLabel.setText("Возраст:");
        ageSpinner = new Spinner(container, SWT.BORDER);
        ageSpinner.setMinimum(1);
        ageSpinner.setMaximum(120);
        ageSpinner.setSelection(person.getAge());

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
        boolean updatedSex = maleButton.getSelection();
        int updatedAge = ageSpinner.getSelection();

        updatedPerson = new Person(person.getId(), updatedName, updatedSex, updatedAge);

        super.okPressed();
    }

    public Person getUpdatedPerson() {
        return updatedPerson;
    }
}