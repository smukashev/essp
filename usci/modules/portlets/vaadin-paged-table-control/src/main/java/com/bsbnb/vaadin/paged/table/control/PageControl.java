package com.bsbnb.vaadin.paged.table.control;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.Reindeer;

/**
 *
 * @author Aidar.Myrzahanov
 */
class PageControl<T> extends HorizontalLayout {

    PageControl(PagingHandler<T> handler) {
        initControls(handler);
    }

    private void initControls(final PagingHandler<T> handler) {
        final TextField currentPageTextField = new TextField();
        currentPageTextField.setValue("1");
        currentPageTextField.addValidator(new IntegerValidator(null));

        currentPageTextField.setStyleName(Reindeer.TEXTFIELD_SMALL);
        currentPageTextField.setImmediate(true);
        currentPageTextField.addListener(new ValueChangeListener() {
            private static final long serialVersionUID = -2255853716069800092L;

            @Override
            public void valueChange(ValueChangeEvent event) {
                if (currentPageTextField.isValid()
                        && currentPageTextField.getValue() != null) {
                    int page = Integer.valueOf(String.valueOf(currentPageTextField.getValue()));
                    handler.setPage(page - 1);
                }
            }
        });
        currentPageTextField.setWidth("50px");
        Label separatorLabel = new Label("/");
        separatorLabel.setWidth(null);
        final Label totalPagesLabel = new Label("1");
        totalPagesLabel.setWidth(null);
        final Button first = new Button("<<", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                handler.setPage(0);
            }
        });
        final Button previous = new Button("<", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                handler.setPage(handler.getCurrentPage() - 1);
            }
        });
        final Button next = new Button(">", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                handler.setPage(handler.getCurrentPage() + 1);
            }
        });
        final Button last = new Button(">>", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                handler.setPage(handler.getPagesCount() - 1);
            }
        });
        first.setStyleName(Reindeer.BUTTON_LINK);
        previous.setStyleName(Reindeer.BUTTON_LINK);
        next.setStyleName(Reindeer.BUTTON_LINK);
        last.setStyleName(Reindeer.BUTTON_LINK);

        addComponent(first);
        addComponent(previous);
        addComponent(currentPageTextField);
        addComponent(separatorLabel);
        addComponent(totalPagesLabel);
        addComponent(next);
        addComponent(last);
        setComponentAlignment(first, Alignment.MIDDLE_LEFT);
        setComponentAlignment(previous, Alignment.MIDDLE_LEFT);
        setComponentAlignment(currentPageTextField, Alignment.MIDDLE_LEFT);
        setComponentAlignment(separatorLabel, Alignment.MIDDLE_LEFT);
        setComponentAlignment(totalPagesLabel, Alignment.MIDDLE_LEFT);
        setComponentAlignment(next, Alignment.MIDDLE_LEFT);
        setComponentAlignment(last, Alignment.MIDDLE_LEFT);
        setWidth(null);
        setSpacing(true);
        handler.addListener(new PageChangedListener() {
            @Override
            public void pageChanged(int pageIndex, int pagesCount) {
                if (pagesCount <= 1) {
                    setVisible(false);
                    return;
                }
                setVisible(true);
                first.setEnabled(pageIndex > 0);
                previous.setEnabled(pageIndex > 0);
                next.setEnabled(pageIndex < pagesCount - 1);
                last.setEnabled(pageIndex < pagesCount - 1);
                currentPageTextField.setValue(String.valueOf(pageIndex + 1));
                totalPagesLabel.setValue(pagesCount);
            }
        });
    }

}
