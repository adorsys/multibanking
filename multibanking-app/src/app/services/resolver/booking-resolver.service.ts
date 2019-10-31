import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap, take } from 'rxjs/operators';
import { ResourceBooking } from '../../../multibanking-api/resourceBooking';
import { getHierarchicalRouteParam } from '../../utils/utils';
import { BookingService } from './../rest/booking.service';

@Injectable({
  providedIn: 'root'
})
export class BookingResolverService {

  private selectedBooking: ResourceBooking;

  constructor(private bookingService: BookingService,
              private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<ResourceBooking> | Observable<never> {
    const accessId = getHierarchicalRouteParam(route, 'access-id');
    const accountId = getHierarchicalRouteParam(route, 'account-id');
    const bookingId = getHierarchicalRouteParam(route, 'booking-id');
    return this.getBooking(accessId, accountId, bookingId);
  }

  public getBooking(accessId: string, accountId: string, bookingId: string) {
    if (this.selectedBooking && bookingId === this.selectedBooking.id) {
      return of(this.selectedBooking);
    }

    if (!accessId || !accountId || !bookingId) {
      return EMPTY;
    }

    console.log('load booking');

    return this.bookingService.getBooking(accessId, accountId, bookingId).pipe(
      take(1),
      mergeMap(booking => {
        if (booking) {
          this.selectedBooking = booking;
          return of(booking);
        } else { // id not found
          this.router.navigate(['/']);
          return EMPTY;
        }
      })
    );
  }
}
