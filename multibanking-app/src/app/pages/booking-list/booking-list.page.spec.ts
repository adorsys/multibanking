import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BookingListPage } from './booking-list.page';

describe('BookingListPage', () => {
  let component: BookingListPage;
  let fixture: ComponentFixture<BookingListPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BookingListPage ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BookingListPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
