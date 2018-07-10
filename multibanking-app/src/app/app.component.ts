import { Component, ViewChild } from "@angular/core";
import { NavController, Platform, Nav } from "ionic-angular";
import { StatusBar } from "@ionic-native/status-bar";
import { SplashScreen } from "@ionic-native/splash-screen";
import { BankAccessListPage } from "../pages/bankaccess/bankAccessList.component";
import { KeycloakService } from "../auth/keycloak.service";
import { CategoriesPage } from "../pages/categories/categories.component";
import { ConfigTabsPage } from "../pages/config-tabs/config-tabs.component";

@Component({
  templateUrl: 'app.html'
})
export class MyApp {

  @ViewChild(Nav) nav: Nav;
  @ViewChild('content') content: NavController;

  backAccessListPage: any = BankAccessListPage;
  categoriesPage: any = CategoriesPage;
  configTabsPage: any = ConfigTabsPage;

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
      KeycloakService.init({ onLoad: 'check-sso', checkLoginIframe: false, adapter: 'default' }).then(() => {
        if (keycloak.authenticated()) {
          this.nav.setRoot(this.backAccessListPage);
        } else {
          keycloak.login();
        }
      });
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

