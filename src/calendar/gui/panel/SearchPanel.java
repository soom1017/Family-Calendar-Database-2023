package calendar.gui.panel;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import calendar.data.dao.EventDao;
import calendar.data.model.Event;
import calendar.gui.elements.DateSpinner;
import calendar.gui.elements.FamilyMemberDropdown;
import calendar.gui.popup.ShowEventDialog;
import calendar.gui.resources.font.CustomFont;

public class SearchPanel extends JPanel {
    private FamilyMemberDropdown attendeeDropdown;
    private DateSpinner dateSpinner;
    private JButton searchButton;
    private JPanel resultPanel;

    private String eventName;
    private boolean detailSearch;
    private List<Event> resultEventList;

    public SearchPanel(String inputText) {
        this.eventName = inputText;
        this.detailSearch = false;
        setLayout(new BorderLayout());

        // Input form
        JPanel inputPanel = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        JLabel notificationLabel = new JLabel("세부 검색", SwingConstants.LEFT);
        notificationLabel.setFont(CustomFont.HEADER_FONT);
        inputPanel.add(notificationLabel, constraints);

        constraints.gridy++;
        constraints.gridwidth = 1;
        JLabel nameLabel = new JLabel("제목: ");
        nameLabel.setFont(CustomFont.DEFAULT_FONT);
        inputPanel.add(nameLabel, constraints);

        constraints.gridx++;
        JLabel nameTextLabel = new JLabel(eventName);
        nameTextLabel.setFont(CustomFont.DEFAULT_FONT);
        inputPanel.add(nameTextLabel, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        JLabel attendLabel = new JLabel("참석자: ");
        attendLabel.setFont(CustomFont.DEFAULT_FONT);
        inputPanel.add(attendLabel, constraints);

        constraints.gridx++;
        attendeeDropdown = new FamilyMemberDropdown();
        inputPanel.add(attendeeDropdown, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        JLabel dateLabel = new JLabel("날짜: ");
        dateLabel.setFont(CustomFont.DEFAULT_FONT);
        inputPanel.add(dateLabel, constraints);

        constraints.gridx = 1;
        dateSpinner = new DateSpinner();
        inputPanel.add(dateSpinner, constraints);

        constraints.gridx = 4;
        constraints.gridy++;
        searchButton = new JButton(new ImageIcon("calendar/gui/resources/icon/search.png"));
        searchButton.addActionListener(e -> requestSearch(true));
        CustomFont.applyButtonStyles(searchButton);
        inputPanel.add(searchButton, constraints);

        add(inputPanel, BorderLayout.NORTH);

        // Output result form
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));

        requestSearch(false);
        
        JScrollPane scrollPane = new JScrollPane(resultPanel);
        scrollPane.setPreferredSize(new Dimension(200, scrollPane.getHeight()));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void requestSearch(boolean isDetailSearch) {
        int attendeeId = attendeeDropdown.getSelectedUserId();
        Date date = dateSpinner.getDate();
        this.detailSearch = true;

        resultPanel.removeAll();

        EventDao eventDao = new EventDao();
        if (isDetailSearch) {
            resultEventList = eventDao.getEventsByEventDetails(eventName, attendeeId, date);
        } else {
            resultEventList = eventDao.getEventsByEventName(eventName);
        }
        if (resultEventList != null && !(resultEventList.isEmpty())) {
            for (Event event: resultEventList) {
                JPanel eventCard = new SearchResultEventCard(event);
                eventCard.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        showEvent(event);
                    }
                });
                resultPanel.add(eventCard);
            }
        }

        resultPanel.revalidate();
        resultPanel.repaint();
    }

    private void showEvent(Event event) {
        ShowEventDialog showEventDialog = new ShowEventDialog(event);
        showEventDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                requestSearch(detailSearch);
            }
        });
        showEventDialog.setVisible(true);
    }
}

class SearchResultEventCard extends JPanel {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");

    public SearchResultEventCard(Event event) {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        JLabel eventNameLabel = new JLabel(event.getEventName(), SwingConstants.LEFT);
        eventNameLabel.setFont(CustomFont.HEADER_FONT);
        add(eventNameLabel, gbc);

        gbc.gridx += 3;
        gbc.gridwidth = 1;
        add(Box.createHorizontalStrut(30), gbc);

        gbc.gridx++;
        gbc.gridy++;
        JLabel allDayLabel = new JLabel(event.isAllDay() ? "하루 종일" : "", SwingConstants.RIGHT);
        allDayLabel.setFont(CustomFont.DETAIL_FONT);
        allDayLabel.setForeground(Color.RED);
        add(allDayLabel, gbc);

        gbc.gridy++;
        String formattedDate = dateFormat.format(new Date(event.getStartAt()));
        JLabel dateTextLabel = new JLabel(formattedDate, SwingConstants.RIGHT);
        dateTextLabel.setFont(CustomFont.DETAIL_FONT);
        add(dateTextLabel, gbc);
    }
}
