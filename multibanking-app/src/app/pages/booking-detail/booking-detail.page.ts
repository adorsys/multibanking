import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BankAccountService } from 'src/app/services/rest/bankAccount.service';
import { ResourceBooking } from 'src/multibanking-api/resourceBooking';
import { RulesService } from '../../services/rest/rules.service';
import { getHierarchicalRouteParam } from '../../utils/utils';

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
    this.bankAccessId = getHierarchicalRouteParam(this.activatedRoute.snapshot, 'access-id');
    this.bankAccountId = getHierarchicalRouteParam(this.activatedRoute.snapshot, 'account-id');
    this.booking = this.activatedRoute.snapshot.data.booking;
  }

}
