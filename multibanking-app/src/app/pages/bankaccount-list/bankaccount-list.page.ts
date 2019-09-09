import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ResourceBankAccount } from 'src/multibanking-api/resourceBankAccount';
import { NavController } from '@ionic/angular';

@Component({
  selector: 'app-bankaccount-list',
  templateUrl: './bankaccount-list.page.html',
  styleUrls: ['./bankaccount-list.page.scss'],
})
export class BankaccountListPage implements OnInit {

  bankAccessId: string;
  bankAccounts: ResourceBankAccount[];

  constructor(private activatedRoute: ActivatedRoute,
              private navCtrl: NavController) { }

  ngOnInit() {
    this.bankAccessId = this.activatedRoute.snapshot.paramMap.get('access-id');
    this.bankAccounts = this.activatedRoute.snapshot.data.bankAccounts;
  }

  itemTapped(bankAccount: ResourceBankAccount) {
    this.navCtrl.navigateForward([`/bankconnections/${this.bankAccessId}/accounts/${bankAccount.id}`]);
  }

}
