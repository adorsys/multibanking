import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BookingGroupsPage } from './booking-groups.page';

describe('BookingGroupsPage', () => {
  let component: BookingGroupsPage;
  let fixture: ComponentFixture<BookingGroupsPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BookingGroupsPage ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BookingGroupsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
