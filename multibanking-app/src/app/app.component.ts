import { Component, ViewChild } from "@angular/core";
import { NavController, Platform } from "ionic-angular";
import { StatusBar } from "@ionic-native/status-bar";
import { SplashScreen } from "@ionic-native/splash-screen";
import { BankAccessListPage } from "../pages/bankaccess/bankAccessList.component";
import { RulesTabsPage } from "../pages/rules-tabs/rules-tabs.component";
import { KeycloakService } from "../auth/keycloak.service";

@Component({
  templateUrl: 'app.html'
})
export class MyApp {

  @ViewChild('content') content: NavController;

  rootPage: any = BankAccessListPage;
  rulesPage: any = RulesTabsPage;

  constructor(
    platform: Platform,
    statusBar: StatusBar,
    splashScreen: SplashScreen,
    private keycloak: KeycloakService
  ) {
    platform.ready().then(() => {
      // Okay, so the platform is ready and our plugins are available.
      // Here you can do any higher level native things you might need.
      statusBar.styleDefault();
      splashScreen.hide();
    });
  }

  isRulesAdmin() {
    return this.keycloak.getRoles().filter(role => role == 'rules_admin').length > 0
  }

  openPage(page) {
    this.content.setRoot(page);
  }

  logout() {
    this.keycloak.logout();
  }
}

