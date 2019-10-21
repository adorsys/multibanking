import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BookingDetailPage } from './booking-detail.page';

describe('BookingDetailPage', () => {
  let component: BookingDetailPage;
  let fixture: ComponentFixture<BookingDetailPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BookingDetailPage ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BookingDetailPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
