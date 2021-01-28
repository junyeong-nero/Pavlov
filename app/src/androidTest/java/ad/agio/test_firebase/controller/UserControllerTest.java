package ad.agio.test_firebase.controller;

import com.google.firebase.firestore.FirebaseFirestore;

import junit.framework.TestCase;

import org.junit.Test;

import ad.agio.test_firebase.domain.User;

public class UserControllerTest extends TestCase {

    @Test
    public void writeUser() {
        UserController controller = new UserController();

        User user = new User();
        user.setAge(21);
        user.setUserName("혜엉");
        user.setEmail("test@email");
        user.setId("ASDFAVACQWERASDGASDF");
        user.setPassword("password");
        controller.writeNewUser(user);
    }
}