import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BankaccessListPage } from './bankaccess-list.page';

describe('BankaccessListPage', () => {
  let component: BankaccessListPage;
  let fixture: ComponentFixture<BankaccessListPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BankaccessListPage ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BankaccessListPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
