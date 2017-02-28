import {Component} from '@angular/core';
import {NavController} from 'ionic-angular';
import {UserService} from '../../services/UserService';
import {LoginPage} from "../login/login";

@Component({
    selector: 'page-register',
    templateUrl: 'register.html'
})
export class RegisterPage {

    user = {id: ''};

    constructor(public navCtrl: NavController, private userService: UserService) {
    }

    public register() {
        this.userService.crateUser(this.user).subscribe( response => {
            this.navCtrl.setRoot(LoginPage);
        })
    }

}
