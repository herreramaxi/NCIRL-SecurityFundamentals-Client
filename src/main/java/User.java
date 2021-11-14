
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Maximiliano Herrera
 */
public class User {

    private String _firstName;
    private String _lastname;
    private String _email;
    private String _pasword;
    private String _data;

    public User(String _firstName, String _lastname, String _email, String _pasword, String _data) {
        this._firstName = _firstName;
        this._lastname = _lastname;
        this._email = _email;
        this._pasword = _pasword;
        this._data = _data;
    }

    public String getFirstName() {
        return _firstName;
    }

    public void setFirstName(String _firstName) {
        this._firstName = _firstName;
    }

    public String getLastname() {
        return _lastname;
    }

    public void setLastname(String _lastname) {
        this._lastname = _lastname;
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String _email) {
        this._email = _email;
    }

    public String getPasword() {
        return _pasword;
    }

    public void setPasword(String _pasword) {
        this._pasword = _pasword;
    }

    public String getData() {
        return _data;
    }

    public void setData(String _data) {
        this._data = _data;
    }
    
    @Override
    public String toString() {
        return "User{" + "_firstName=" + _firstName + ", _lastname=" + _lastname + ", _email=" + _email + ", _pasword=" + _pasword + ", _data=" + _data + '}';
    }
}
