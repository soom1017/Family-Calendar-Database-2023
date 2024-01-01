package calendar.gui.elements;

import java.awt.Component;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

import calendar.data.dao.UserDao;
import calendar.data.model.User;
import calendar.gui.resources.font.CustomFont;

public class FamilyMemberDropdown extends JComboBox<User> {
    List<User> familyMemberList;

    public FamilyMemberDropdown() {
        super();

        UserDao userDao = new UserDao();
        familyMemberList = userDao.getFamilyMembers();

        if (familyMemberList != null && !familyMemberList.isEmpty()) {
            User defaultOption = new User();
            defaultOption.setUserName("");
            defaultOption.setEmail("선택 안함");
            familyMemberList.add(0, defaultOption);

            setModel(new DefaultComboBoxModel<>(familyMemberList.toArray(new User[0])));
            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                            boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof User) {
                        User user = (User) value;
                        setText(user.getUserName() + " (" + user.getEmail() + ")");
                    }
                    return this;
                }
            });
        }
        setFont(CustomFont.DEFAULT_FONT);
    }

    public int getSelectedUserId() {
        User selectedUser = (User) getSelectedItem();
        if (selectedUser == null)
            return -1;
        return selectedUser.getUserId();
    }
}

