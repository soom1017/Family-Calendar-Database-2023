package calendar.gui.elements;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JTextField;

import calendar.gui.listener.SearchListener;
import calendar.gui.resources.font.CustomFont;

public class SearchEventTextField extends JTextField {
    private ActionListener enterAction;
    private SearchListener searchListener;

    public SearchEventTextField(int columns) {
        super(columns);
        setFont(CustomFont.DEFAULT_FONT);
        setToolTipText("이벤트 제목으로 검색...");

        enterAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String enteredEventName = getText();
                if (searchListener != null && !enteredEventName.isEmpty())
                    searchListener.onSearch(enteredEventName);
            }
        };

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getText().equals(getToolTipText())) {
                    setText("");
                }
                addActionListener(enterAction);
            }

            @Override
            public void focusLost(FocusEvent e) {
                removeActionListener(enterAction);
                if (getText().isEmpty()) {
                    setText(getToolTipText());
                }
            }
        });
        
        setText(getToolTipText());
    }

    public void addSearchListener(SearchListener listener) {
        this.searchListener = listener;
    }
}

