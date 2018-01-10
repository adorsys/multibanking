import { Component, ViewChild } from "@angular/core";
import { NavController, Platform } from "ionic-angular";
import { StatusBar } from "@ionic-native/status-bar";
import { SplashScreen } from "@ionic-native/splash-screen";
import { BankAccessListPage } from "../pages/bankaccess/bankAccessList.component";
import { RulesCustomPage } from "../pages/rules-custom/rulesCustom.component";

@Component({
  templateUrl: 'app.html'
})
export class MyApp {

  @ViewChild('content') content: NavController;

  rootPage: any = BankAccessListPage;
  rulesPage: any = RulesCustomPage;

  constructor(
    platform: Platform,
    statusBar: StatusBar,
    splashScreen: SplashScreen
  ) {
    platform.ready().then(() => {
      // Okay, so the platform is ready and our plugins are available.
      // Here you can do any higher level native things you might need.
      statusBar.styleDefault();
      splashScreen.hide();
    });
  }

  openPage(page) {
    this.content.setRoot(page);
  }
}

