import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BankaccessEditPage } from './bankaccess-edit.page';

describe('BankaccessEditPage', () => {
  let component: BankaccessEditPage;
  let fixture: ComponentFixture<BankaccessEditPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BankaccessEditPage ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BankaccessEditPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
