import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BankAccountService } from 'src/app/services/rest/bankAccount.service';
import { ResourceBooking } from 'src/multibanking-api/resourceBooking';
import { RulesService } from '../../services/rest/rules.service';

@Component({
  selector: 'app-booking-detail',
  templateUrl: './booking-detail.page.html',
  styleUrls: ['./booking-detail.page.scss'],
})
export class BookingDetailPage implements OnInit {

  bankAccessId: string;
  bankAccountId: string;
  booking: ResourceBooking;

  constructor(private activatedRoute: ActivatedRoute,
              private bankAccountService: BankAccountService,
              private rulesService: RulesService) {
  }

  ngOnInit() {
    this.bankAccessId = this.activatedRoute.snapshot.paramMap.get('access-id');
    this.bankAccountId = this.activatedRoute.snapshot.paramMap.get('account-id');
    this.booking = this.activatedRoute.snapshot.data.booking;
    console.log(this.activatedRoute.snapshot.data.booking);
  }

}
