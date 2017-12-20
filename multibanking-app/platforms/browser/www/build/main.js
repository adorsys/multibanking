webpackJsonp([0],{

/***/ 151:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return AnalyticsService; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__app_app_config__ = __webpack_require__(23);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__angular_http__ = __webpack_require__(33);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};




var AnalyticsService = /** @class */ (function () {
    function AnalyticsService(http) {
        this.http = http;
    }
    AnalyticsService.prototype.getAvailableCategories = function () {
        return this.http.get(__WEBPACK_IMPORTED_MODULE_1__app_app_config__["a" /* AppConfig */].api_url + "/analytics/categories")
            .map(function (res) { return res.json()._embedded.ruleCategoryList; })
            .catch(this.handleError);
    };
    AnalyticsService.prototype.createRule = function (rule) {
        return this.http.post(__WEBPACK_IMPORTED_MODULE_1__app_app_config__["a" /* AppConfig */].api_url + "/analytics/rules", rule)
            .catch(this.handleError);
    };
    AnalyticsService.prototype.getAnalytics = function (accessId, accountId) {
        return this.http.get(__WEBPACK_IMPORTED_MODULE_1__app_app_config__["a" /* AppConfig */].api_url + "/bankaccesses/" + accessId + "/accounts/" + accountId + "/analytics")
            .map(function (res) { return res.json() != null ? res.json() : {}; })
            .catch(this.handleError);
    };
    AnalyticsService.prototype.handleError = function (error) {
        console.error(error);
        var errorJson = error.json();
        if (errorJson) {
            if (errorJson.message == "RESCOURCE_NOT_FOUND") {
                return __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__["Observable"].of({});
            }
            else if (errorJson.message == "SYNC_IN_PROGRESS") {
                return __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__["Observable"].throw(errorJson.message);
            }
            else {
                return __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__["Observable"].throw(errorJson || 'Server error');
            }
        }
        else {
            return __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__["Observable"].throw(error || 'Server error');
        }
    };
    AnalyticsService = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["A" /* Injectable */])(),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_2__angular_http__["c" /* Http */]])
    ], AnalyticsService);
    return AnalyticsService;
}());

//# sourceMappingURL=analyticsService.js.map

/***/ }),

/***/ 152:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return LogoService; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__app_app_config__ = __webpack_require__(23);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};


var LogoService = /** @class */ (function () {
    function LogoService() {
    }
    LogoService.prototype.getLogo = function (logoId) {
        if (!logoId) {
            __WEBPACK_IMPORTED_MODULE_1__app_app_config__["a" /* AppConfig */].api_url + "/image/keinlogo_256";
        }
        return __WEBPACK_IMPORTED_MODULE_1__app_app_config__["a" /* AppConfig */].api_url + "/image/" + logoId;
    };
    LogoService = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["A" /* Injectable */])(),
        __metadata("design:paramtypes", [])
    ], LogoService);
    return LogoService;
}());

//# sourceMappingURL=LogoService.js.map

/***/ }),

/***/ 153:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return KeycloakService; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__app_app_config__ = __webpack_require__(23);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};


var KeycloakService = /** @class */ (function () {
    function KeycloakService() {
    }
    KeycloakService_1 = KeycloakService;
    KeycloakService.init = function () {
        var keycloakAuth = Keycloak({
            url: __WEBPACK_IMPORTED_MODULE_1__app_app_config__["a" /* AppConfig */].auth_url,
            realm: 'multibanking',
            clientId: 'multibanking-client',
        });
        KeycloakService_1.auth.loggedIn = false;
        return new Promise(function (resolve, reject) {
            keycloakAuth.init({ onLoad: 'login-required' })
                .success(function () {
                KeycloakService_1.auth.loggedIn = true;
                KeycloakService_1.auth.authz = keycloakAuth;
                KeycloakService_1.auth.logoutUrl = keycloakAuth.authServerUrl
                    + '/realms/multibanking/protocol/openid-connect/logout?redirect_uri='
                    + document.baseURI;
                resolve();
            })
                .error(function () {
                reject();
            });
        });
    };
    KeycloakService.prototype.logout = function () {
        console.log('*** LOGOUT');
        KeycloakService_1.auth.loggedIn = false;
        KeycloakService_1.auth.authz = null;
        window.location.href = KeycloakService_1.auth.logoutUrl;
    };
    KeycloakService.prototype.getToken = function () {
        return new Promise(function (resolve, reject) {
            if (KeycloakService_1.auth.authz.token) {
                KeycloakService_1.auth.authz
                    .updateToken(5)
                    .success(function () {
                    resolve(KeycloakService_1.auth.authz.token);
                })
                    .error(function () {
                    KeycloakService_1.auth.authz.login();
                });
            }
            else {
                reject('Not loggen in');
            }
        });
    };
    KeycloakService.prototype.getUsername = function () {
        return KeycloakService_1.auth.authz.tokenParsed.sub;
    };
    KeycloakService.auth = {};
    KeycloakService = KeycloakService_1 = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["A" /* Injectable */])()
    ], KeycloakService);
    return KeycloakService;
    var KeycloakService_1;
}());

//# sourceMappingURL=keycloak.service.js.map

/***/ }),

/***/ 166:
/***/ (function(module, exports) {

function webpackEmptyAsyncContext(req) {
	// Here Promise.resolve().then() is used instead of new Promise() to prevent
	// uncatched exception popping up in devtools
	return Promise.resolve().then(function() {
		throw new Error("Cannot find module '" + req + "'.");
	});
}
webpackEmptyAsyncContext.keys = function() { return []; };
webpackEmptyAsyncContext.resolve = webpackEmptyAsyncContext;
module.exports = webpackEmptyAsyncContext;
webpackEmptyAsyncContext.id = 166;

/***/ }),

/***/ 23:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return AppConfig; });
var AppConfig = {
    production: false,
    // Openshift
    // auth_url: 'https://multibanking-keycloak.dev.adorsys.de/auth',
    // api_url: 'https://multibanking-service.dev.adorsys.de/api/v1'
    // multibanking service running from docker container
    // auth_url: 'http://keycloak:8080/auth',
    // api_url: 'http://localhost:8081/api/v1'
    // multibanking service running from ide
    auth_url: 'http://localhost:8080/auth',
    api_url: 'http://localhost:8081/api/v1'
};
//# sourceMappingURL=app.config.js.map

/***/ }),

/***/ 305:
/***/ (function(module, exports) {

function webpackEmptyAsyncContext(req) {
	// Here Promise.resolve().then() is used instead of new Promise() to prevent
	// uncatched exception popping up in devtools
	return Promise.resolve().then(function() {
		throw new Error("Cannot find module '" + req + "'.");
	});
}
webpackEmptyAsyncContext.keys = function() { return []; };
webpackEmptyAsyncContext.resolve = webpackEmptyAsyncContext;
module.exports = webpackEmptyAsyncContext;
webpackEmptyAsyncContext.id = 305;

/***/ }),

/***/ 349:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return AnalyticsPage; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_ionic_angular__ = __webpack_require__(20);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__services_bankAccountService__ = __webpack_require__(62);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__services_analyticsService__ = __webpack_require__(151);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__app_app_config__ = __webpack_require__(23);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__bookingGroup__ = __webpack_require__(350);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};






var AnalyticsPage = /** @class */ (function () {
    function AnalyticsPage(navCtrl, navparams, alertCtrl, toastCtrl, loadingCtrl, bankAccountService, analyticsService) {
        this.navCtrl = navCtrl;
        this.navparams = navparams;
        this.alertCtrl = alertCtrl;
        this.toastCtrl = toastCtrl;
        this.loadingCtrl = loadingCtrl;
        this.bankAccountService = bankAccountService;
        this.analyticsService = analyticsService;
        this.bankAccess = navparams.data.bankAccess;
        this.bankAccountId = navparams.data.bankAccount.id;
    }
    AnalyticsPage.prototype.ngOnInit = function () {
        var _this = this;
        this.bankAccountService.bookingsChangedObservable.subscribe(function (changed) {
            _this.loadAnalytics();
        });
        this.loadAnalytics();
    };
    AnalyticsPage.prototype.ionViewDidLoad = function () {
        var _this = this;
        this.navBar.backButtonClick = function (e) {
            _this.navCtrl.parent.viewCtrl.dismiss();
        };
    };
    AnalyticsPage.prototype.loadAnalytics = function () {
        var _this = this;
        this.analyticsService.getAnalytics(this.bankAccess.id, this.bankAccountId).subscribe(function (response) {
            _this.analytics = response;
        }, function (error) {
            if (error == "SYNC_IN_PROGRESS") {
                _this.toastCtrl.create({
                    message: 'Account sync in progress',
                    showCloseButton: true,
                    position: 'top'
                }).present();
            }
        });
    };
    AnalyticsPage.prototype.getCompanyLogoUrl = function (bookingGroup) {
        return __WEBPACK_IMPORTED_MODULE_4__app_app_config__["a" /* AppConfig */].api_url + "/image/" + bookingGroup.contract.logo;
    };
    AnalyticsPage.prototype.syncBookingsPromptPin = function () {
        var _this = this;
        var alert = this.alertCtrl.create({
            title: 'Pin',
            inputs: [
                {
                    name: 'pin',
                    placeholder: 'Bank Account Pin',
                    type: 'password'
                }
            ],
            buttons: [
                {
                    text: 'Cancel',
                    role: 'cancel'
                },
                {
                    text: 'Submit',
                    handler: function (data) {
                        if (data.pin.length > 0) {
                            _this.syncBookings(data.pin);
                        }
                    }
                }
            ]
        });
        alert.present();
    };
    AnalyticsPage.prototype.syncBookings = function (pin) {
        var _this = this;
        if (!pin && !this.bankAccess.storePin) {
            return this.syncBookingsPromptPin();
        }
        var loading = this.loadingCtrl.create({
            content: 'Please wait...'
        });
        loading.present();
        this.bankAccountService.syncBookings(this.bankAccess.id, this.bankAccountId, pin).subscribe(function (response) {
            loading.dismiss();
        }, function (error) {
            if (error && error.messages) {
                error.messages.forEach(function (message) {
                    if (message.key == "SYNC_IN_PROGRESS") {
                        _this.toastCtrl.create({
                            message: 'Account sync in progress',
                            showCloseButton: true,
                            position: 'top'
                        }).present();
                    }
                    else if (message.key == "INVALID_PIN") {
                        _this.alertCtrl.create({
                            message: 'Invalid pin',
                            buttons: ['OK']
                        }).present();
                    }
                });
            }
        });
    };
    AnalyticsPage.prototype.itemSelected = function (label, bookingGroups) {
        this.navCtrl.push(__WEBPACK_IMPORTED_MODULE_5__bookingGroup__["a" /* BookingGroupPage */], { label: label, bookingGroups: bookingGroups });
    };
    __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["_8" /* ViewChild */])(__WEBPACK_IMPORTED_MODULE_1_ionic_angular__["h" /* Navbar */]),
        __metadata("design:type", __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["h" /* Navbar */])
    ], AnalyticsPage.prototype, "navBar", void 0);
    AnalyticsPage = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["m" /* Component */])({
            selector: 'page-analytics',template:/*ion-inline-start:"/Users/alexg/git/multibanking/multibanking-app/src/pages/analytics/analytics.html"*/'<ion-header>\n  <ion-navbar class="force-back-button">\n    <ion-title>Analytics {{ analytics?.analyticsDate | date }}</ion-title>\n    <ion-buttons end>\n      <button ion-button (click)="syncBookings()">\n        Reload\n      </button>\n    </ion-buttons>\n  </ion-navbar>\n</ion-header>\n\n<ion-content class="bookingList-content" padding>\n  <ion-item-group>\n    <ion-item-divider color="light">Balance end of month: {{analytics?.balanceCalculated | currency:\'EUR\':true}}</ion-item-divider>\n    <ion-item-divider color="light">Income</ion-item-divider>\n    <ion-item>Total: {{analytics?.incomeTotal | currency:\'EUR\':true}}</ion-item>\n    <ion-item (click)="itemSelected(\'Fix income\', analytics?.incomeFixBookings)">Fix: {{analytics?.incomeFix | currency:\'EUR\':true}}\n      <button ion-button clear item-end>View</button>\n    </ion-item>\n    <ion-item (click)="itemSelected(\'Variable income\', analytics?.incomeVariableBookings)">Variable: {{analytics?.incomeVariable | currency:\'EUR\':true}}\n      <button ion-button clear item-end>View</button>\n    </ion-item>\n    <ion-item (click)="itemSelected(\'Next income\', analytics?.incomeNextBookings)">Next: {{analytics?.incomeNext | currency:\'EUR\':true}}\n      <button ion-button clear item-end>View</button>\n    </ion-item>\n  </ion-item-group>\n  <ion-item-group>\n    <ion-item-divider color="light">Expenses</ion-item-divider>\n    <ion-item>Total: {{analytics?.expensesTotal | currency:\'EUR\':true}}</ion-item>\n    <ion-item (click)="itemSelected(\'Fix expenses\', analytics?.expensesFixBookings)">Fix: {{analytics?.expensesFix | currency:\'EUR\':true}}\n      <button ion-button clear item-end>View</button>\n    </ion-item>\n    <ion-item (click)="itemSelected(\'Variable expenses\', analytics?.expensesVariableBookings)">Variable: {{analytics?.expensesVariable | currency:\'EUR\':true}}\n      <button ion-button clear item-end>View</button>\n    </ion-item>\n    <ion-item (click)="itemSelected(\'Next expenses\', analytics?.expensesNextBookings)">Next: {{analytics?.expensesNext | currency:\'EUR\':true}}\n      <button ion-button clear item-end>View</button>\n    </ion-item>\n  </ion-item-group>\n</ion-content>\n'/*ion-inline-end:"/Users/alexg/git/multibanking/multibanking-app/src/pages/analytics/analytics.html"*/
        }),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_1_ionic_angular__["f" /* NavController */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["g" /* NavParams */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["a" /* AlertController */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["j" /* ToastController */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["e" /* LoadingController */],
            __WEBPACK_IMPORTED_MODULE_2__services_bankAccountService__["a" /* BankAccountService */],
            __WEBPACK_IMPORTED_MODULE_3__services_analyticsService__["a" /* AnalyticsService */]])
    ], AnalyticsPage);
    return AnalyticsPage;
}());

//# sourceMappingURL=analytics.js.map

/***/ }),

/***/ 350:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return BookingGroupPage; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_ionic_angular__ = __webpack_require__(20);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__app_app_config__ = __webpack_require__(23);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};



var BookingGroupPage = /** @class */ (function () {
    function BookingGroupPage(navparams) {
        this.navparams = navparams;
        this.label = navparams.data.label;
        this.bookingGroups = this.sortBookingGroups(navparams.data.bookingGroups);
    }
    BookingGroupPage.prototype.sortBookingGroups = function (bookingGroups) {
        return bookingGroups.sort(function (group1, group2) {
            if (group1.variable == true) {
                return 1;
            }
            if (group2.variable == true) {
                return -1;
            }
            var group1Date = new Date(group1.nextExecutionDate[0], group1.nextExecutionDate[1] - 1, group1.nextExecutionDate[2]);
            var group2Date = new Date(group2.nextExecutionDate[0], group2.nextExecutionDate[1] - 1, group2.nextExecutionDate[2]);
            if (group1Date > group2Date) {
                return -1;
            }
            if (group1Date < group2Date) {
                return 1;
            }
            return 0;
        });
    };
    BookingGroupPage.prototype.getCompanyLogoUrl = function (bookingGroup) {
        return __WEBPACK_IMPORTED_MODULE_2__app_app_config__["a" /* AppConfig */].api_url + "/image/" + bookingGroup.contract.logo;
    };
    BookingGroupPage = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["m" /* Component */])({
            selector: 'page-bookingGroup',template:/*ion-inline-start:"/Users/alexg/git/multibanking/multibanking-app/src/pages/analytics/bookingGroup.html"*/'<ion-header>\n  <ion-navbar>\n    <ion-title>{{ label }}</ion-title>\n  </ion-navbar>\n</ion-header>\n\n<ion-content padding>\n  <ion-item *ngFor="let bookingGroup of bookingGroups">\n    <ion-thumbnail item-left>\n      <img [src]="getCompanyLogoUrl(bookingGroup)" *ngIf="bookingGroup.contract.logo" item-left/>\n    </ion-thumbnail>\n    <h4 *ngIf="bookingGroup.otherAccount">{{bookingGroup.otherAccount}}</h4>\n    <h4 *ngIf="bookingGroup.mainCategory">{{bookingGroup.mainCategory}}</h4>\n    <h4 *ngIf="bookingGroup.subCategory">{{bookingGroup.subCategory}}</h4>\n    <h4 *ngIf="bookingGroup.specification">{{bookingGroup.specification}}</h4>\n    <div item-right>\n      <h4>{{ bookingGroup.nextExecutionDate | date }}</h4>\n      <h2>{{ bookingGroup.amount | currency:\'EUR\':true }}</h2>\n    </div>\n  </ion-item>\n</ion-content>\n'/*ion-inline-end:"/Users/alexg/git/multibanking/multibanking-app/src/pages/analytics/bookingGroup.html"*/
        }),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_1_ionic_angular__["g" /* NavParams */]])
    ], BookingGroupPage);
    return BookingGroupPage;
}());

//# sourceMappingURL=bookingGroup.js.map

/***/ }),

/***/ 351:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return BankAccessCreatePage; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_ionic_angular__ = __webpack_require__(20);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__services_bankAccessService__ = __webpack_require__(86);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_ionic2_auto_complete__ = __webpack_require__(167);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__services_bankAutoCompleteService__ = __webpack_require__(352);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};





var BankAccessCreatePage = /** @class */ (function () {
    function BankAccessCreatePage(navCtrl, navparams, bankAutoCompleteService, loadingCtrl, alertCtrl, bankAccessService) {
        this.navCtrl = navCtrl;
        this.navparams = navparams;
        this.bankAutoCompleteService = bankAutoCompleteService;
        this.loadingCtrl = loadingCtrl;
        this.alertCtrl = alertCtrl;
        this.bankAccessService = bankAccessService;
        this.bankAccess = {
            bankCode: '',
            bankLogin: '',
            bankLogin2: '',
            pin: '',
            userId: '',
            storePin: true,
            storeBookings: true,
            categorizeBookings: true,
            storeAnalytics: true
        };
        this.userId = navparams.data.userId;
        this.bankAccess.userId = navparams.data.userId;
        this.parent = navparams.data.parent;
    }
    BankAccessCreatePage.prototype.ngOnInit = function () {
        var _this = this;
        this.autocomplete.itemSelected.subscribe(function (bank) {
            if (!bank.loginSettings) {
                bank.loginSettings = {
                    advice: "Bank login data",
                    credentials: [{ label: "customer id", masked: false }, { label: "pin", masked: true }]
                };
            }
            _this.selectedBank = bank;
        });
        this.autocomplete.searchbarElem.ionClear.subscribe(function () {
            _this.selectedBank = undefined;
        });
    };
    BankAccessCreatePage.prototype.createBankAccess = function () {
        var _this = this;
        var loading = this.loadingCtrl.create({
            content: 'Please wait...'
        });
        loading.present();
        this.bankAccess.bankCode = this.selectedBank.bankCode;
        for (var i = 0; i < this.selectedBank.loginSettings.credentials.length; i++) {
            if (i == 0) {
                this.bankAccess.bankLogin = this.selectedBank.loginSettings.credentials[i].input;
            }
            else if (i == 1) {
                if (!this.selectedBank.loginSettings.credentials[i].masked) {
                    this.bankAccess.bankLogin2 = this.selectedBank.loginSettings.credentials[i].input;
                }
                else {
                    this.bankAccess.pin = this.selectedBank.loginSettings.credentials[i].input;
                }
            }
            else if (i == 2) {
                this.bankAccess.pin = this.selectedBank.loginSettings.credentials[i].input;
            }
        }
        this.bankAccessService.createBankAcccess(this.bankAccess).subscribe(function (response) {
            loading.dismiss();
            _this.parent.bankAccessesChanged();
            _this.navCtrl.pop();
        }, function (error) {
            loading.dismiss();
            if (error && error.messages) {
                error.messages.forEach(function (message) {
                    if (message.key == "BANK_ACCESS_ALREADY_EXIST") {
                        _this.alertCtrl.create({
                            message: 'Bank connection already exists',
                            buttons: ['OK']
                        }).present();
                    }
                    else if (message.key == "INVALID_BANK_ACCESS") {
                        _this.alertCtrl.create({
                            message: 'Bank not supported',
                            buttons: ['OK']
                        }).present();
                    }
                    else if (message.key == "INVALID_PIN") {
                        _this.alertCtrl.create({
                            message: 'Invalid pin',
                            buttons: ['OK']
                        }).present();
                    }
                });
            }
        });
    };
    __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["_8" /* ViewChild */])('autocomplete'),
        __metadata("design:type", __WEBPACK_IMPORTED_MODULE_3_ionic2_auto_complete__["a" /* AutoCompleteComponent */])
    ], BankAccessCreatePage.prototype, "autocomplete", void 0);
    BankAccessCreatePage = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["m" /* Component */])({
            selector: 'page-bankaccess-create',template:/*ion-inline-start:"/Users/alexg/git/multibanking/multibanking-app/src/pages/bankaccess/bankAccessCreate.html"*/'<ion-header>\n  <ion-navbar>\n    <ion-title>New Bank Connection</ion-title>\n  </ion-navbar>\n</ion-header>\n\n<ion-content class="bankaccess-create-content" padding>\n  <ion-auto-complete [dataProvider]="bankAutoCompleteService" [options]="{placeholder : \'Bank name or bank code\'}" #autocomplete></ion-auto-complete>\n  <form (ngSubmit)="createBankAccess()" #createForm="ngForm" *ngIf="selectedBank">\n\n    <ion-item-group>\n      <ion-item-divider color="light">{{selectedBank.loginSettings.advice ? selectedBank.loginSettings.advice :\'Bank login data\'}}</ion-item-divider>\n      <ion-item *ngFor="let item of selectedBank.loginSettings.credentials">\n        <ion-label floating>{{item.label}}</ion-label>\n        <ion-input type="{{item.masked ? \'password\' : \'text\'}}" name="bankLogin" [(ngModel)]="item.input" required></ion-input>\n      </ion-item>\n    </ion-item-group>\n    <ion-item-group>\n      <ion-item-divider color="light">Bank settings</ion-item-divider>\n      <ion-item>\n        <ion-label>Save Pin</ion-label>\n        <ion-toggle [(ngModel)]="bankAccess.storePin" [ngModelOptions]="{standalone: true}"></ion-toggle>\n      </ion-item>\n\n      <ion-item>\n        <ion-label>Save Bookings</ion-label>\n        <ion-toggle [(ngModel)]="bankAccess.storeBookings" [ngModelOptions]="{standalone: true}"></ion-toggle>\n      </ion-item>\n\n      <ion-item>\n        <ion-label>Categorize Bookings</ion-label>\n        <ion-toggle [(ngModel)]="bankAccess.categorizeBookings" [ngModelOptions]="{standalone: true}"></ion-toggle>\n      </ion-item>\n\n      <ion-item>\n        <ion-label>Save anonymized Bookings</ion-label>\n        <ion-toggle [(ngModel)]="bankAccess.storeAnonymizedBookings" [ngModelOptions]="{standalone: true}"></ion-toggle>\n      </ion-item>\n\n      <ion-item>\n        <ion-label>Save Analytics</ion-label>\n        <ion-toggle [(ngModel)]="bankAccess.storeAnalytics" [ngModelOptions]="{standalone: true}"></ion-toggle>\n      </ion-item>\n\n    </ion-item-group>\n    <button ion-button color="primary" block type="submit" [disabled]="!createForm.form.valid">Submit</button>\n\n  </form>\n</ion-content>\n'/*ion-inline-end:"/Users/alexg/git/multibanking/multibanking-app/src/pages/bankaccess/bankAccessCreate.html"*/
        }),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_1_ionic_angular__["f" /* NavController */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["g" /* NavParams */],
            __WEBPACK_IMPORTED_MODULE_4__services_bankAutoCompleteService__["a" /* BankAutoCompleteService */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["e" /* LoadingController */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["a" /* AlertController */],
            __WEBPACK_IMPORTED_MODULE_2__services_bankAccessService__["a" /* BankAccessService */]])
    ], BankAccessCreatePage);
    return BankAccessCreatePage;
}());

//# sourceMappingURL=bankAccessCreate.js.map

/***/ }),

/***/ 352:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return BankAutoCompleteService; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__app_app_config__ = __webpack_require__(23);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__angular_http__ = __webpack_require__(33);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};




var BankAutoCompleteService = /** @class */ (function () {
    function BankAutoCompleteService(http) {
        this.http = http;
        this.labelAttribute = "name";
    }
    BankAutoCompleteService.prototype.getResults = function (keyword) {
        return this.http.get(__WEBPACK_IMPORTED_MODULE_1__app_app_config__["a" /* AppConfig */].api_url + "/bank?query=" + keyword)
            .map(function (res) {
            return res.json()._embedded != null ? res.json()._embedded.bankEntityList : [];
        })
            .catch(this.handleError);
    };
    BankAutoCompleteService.prototype.handleError = function (error) {
        console.error(error);
        var errorJson = error.json();
        if (errorJson) {
            return __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__["Observable"].throw(errorJson || 'Server error');
        }
        else {
            return __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__["Observable"].throw(error || 'Server error');
        }
    };
    BankAutoCompleteService = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["A" /* Injectable */])(),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_2__angular_http__["c" /* Http */]])
    ], BankAutoCompleteService);
    return BankAutoCompleteService;
}());

//# sourceMappingURL=bankAutoCompleteService.js.map

/***/ }),

/***/ 353:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return BankAccessListPage; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_ionic_angular__ = __webpack_require__(20);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__bankaccount_bankaccountList__ = __webpack_require__(354);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__bankAccessCreate__ = __webpack_require__(351);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__services_bankAccessService__ = __webpack_require__(86);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__bankAccessUpdate__ = __webpack_require__(364);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};






var BankAccessListPage = /** @class */ (function () {
    function BankAccessListPage(navCtrl, bankAccessService) {
        this.navCtrl = navCtrl;
        this.bankAccessService = bankAccessService;
    }
    BankAccessListPage.prototype.ngOnInit = function () {
        var _this = this;
        this.bankAccessService.getBankAccesses().subscribe(function (response) {
            _this.bankaccesses = response;
        });
    };
    BankAccessListPage.prototype.itemSelected = function (bankAccess) {
        this.navCtrl.push(__WEBPACK_IMPORTED_MODULE_2__bankaccount_bankaccountList__["a" /* BankAccountListPage */], {
            bankAccess: bankAccess,
        });
    };
    BankAccessListPage.prototype.createBankAccess = function () {
        this.navCtrl.push(__WEBPACK_IMPORTED_MODULE_3__bankAccessCreate__["a" /* BankAccessCreatePage */], { parent: this });
    };
    BankAccessListPage.prototype.bankAccessesChanged = function () {
        var _this = this;
        this.bankAccessService.getBankAccesses().subscribe(function (response) {
            _this.bankaccesses = response;
        });
    };
    BankAccessListPage.prototype.editBankAccess = function ($event, bankAccess) {
        $event.stopPropagation();
        this.navCtrl.push(__WEBPACK_IMPORTED_MODULE_5__bankAccessUpdate__["a" /* BankAccessUpdatePage */], { bankAccess: bankAccess, parent: this });
    };
    BankAccessListPage.prototype.deleteBankAccess = function ($event, bankAccess) {
        var _this = this;
        $event.stopPropagation();
        this.bankAccessService.deleteBankAccess(bankAccess.id).subscribe(function (response) {
            _this.bankAccessesChanged();
        });
    };
    BankAccessListPage = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["m" /* Component */])({
            selector: 'page-bankaccessList',template:/*ion-inline-start:"/Users/alexg/git/multibanking/multibanking-app/src/pages/bankaccess/bankAccessList.html"*/'<ion-header>\n  <ion-navbar>\n    <ion-title>Bank Connections</ion-title>\n  </ion-navbar>\n</ion-header>\n\n<ion-content class="bankaccessList-content" padding>\n  <ion-fab top right edge>\n    <button ion-fab mini (click)="createBankAccess()"><ion-icon name="add"></ion-icon></button>\n  </ion-fab>\n  <ion-list>\n    <ion-item *ngFor="let bankaccess of bankaccesses" (click)="itemSelected(bankaccess)">\n      <h3>{{ bankaccess.bankCode }}</h3>\n      <h2>{{ bankaccess.bankName }}</h2>\n      <button ion-button outline item-end (click)="editBankAccess($event, bankaccess)">\n        <ion-icon name="settings"></ion-icon>\n        Edit\n      </button>\n      <button ion-button outline item-end (click)="deleteBankAccess($event, bankaccess)">\n        <ion-icon name="trash"></ion-icon>\n        Delete\n      </button>\n    </ion-item>\n  </ion-list>\n</ion-content>\n'/*ion-inline-end:"/Users/alexg/git/multibanking/multibanking-app/src/pages/bankaccess/bankAccessList.html"*/
        }),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_1_ionic_angular__["f" /* NavController */],
            __WEBPACK_IMPORTED_MODULE_4__services_bankAccessService__["a" /* BankAccessService */]])
    ], BankAccessListPage);
    return BankAccessListPage;
}());

//# sourceMappingURL=bankAccessList.js.map

/***/ }),

/***/ 354:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return BankAccountListPage; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_ionic_angular__ = __webpack_require__(20);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__services_bankAccountService__ = __webpack_require__(62);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__tabs_tabs__ = __webpack_require__(355);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};




var BankAccountListPage = /** @class */ (function () {
    function BankAccountListPage(navCtrl, navparams, bankAccountService) {
        this.navCtrl = navCtrl;
        this.navparams = navparams;
        this.bankAccountService = bankAccountService;
        this.bankAccess = navparams.data.bankAccess;
    }
    BankAccountListPage.prototype.ngOnInit = function () {
        var _this = this;
        this.loadBankAccounts();
        this.bankAccountService.bookingsChangedObservable.subscribe(function (changed) {
            _this.loadBankAccounts();
        });
    };
    BankAccountListPage.prototype.loadBankAccounts = function () {
        var _this = this;
        this.bankAccountService.getBankAccounts(this.bankAccess.id).subscribe(function (response) {
            _this.bankAccounts = response;
        });
    };
    BankAccountListPage.prototype.itemSelected = function (bankAccount) {
        this.navCtrl.push(__WEBPACK_IMPORTED_MODULE_3__tabs_tabs__["a" /* TabsPage */], {
            bankAccess: this.bankAccess,
            bankAccount: bankAccount,
        });
    };
    BankAccountListPage = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["m" /* Component */])({
            selector: 'page-bankaccountList',template:/*ion-inline-start:"/Users/alexg/git/multibanking/multibanking-app/src/pages/bankaccount/bankaccountList.html"*/'<ion-header>\n  <ion-navbar>\n    <ion-title>Bank Accounts</ion-title>\n  </ion-navbar>\n\n</ion-header>\n\n<ion-content class="bankaccountList-content" padding>\n  <ion-list>\n    <button ion-item *ngFor="let bankAccount of bankAccounts" (click)="itemSelected(bankAccount)">\n      <h3>{{ bankAccount.accountNumber }}</h3>\n      <h2>{{ bankAccount.type }}</h2>\n      <h2 *ngIf="bankAccount.bankAccountBalance" item-right>{{ bankAccount.bankAccountBalance.readyHbciBalance | currency:\'EUR\':true }}</h2>\n    </button>\n  </ion-list>\n</ion-content>\n'/*ion-inline-end:"/Users/alexg/git/multibanking/multibanking-app/src/pages/bankaccount/bankaccountList.html"*/
        }),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_1_ionic_angular__["f" /* NavController */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["g" /* NavParams */],
            __WEBPACK_IMPORTED_MODULE_2__services_bankAccountService__["a" /* BankAccountService */]])
    ], BankAccountListPage);
    return BankAccountListPage;
}());

//# sourceMappingURL=bankaccountList.js.map

/***/ }),

/***/ 355:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return TabsPage; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__booking_bookingList__ = __webpack_require__(356);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__contracts_contracts_component__ = __webpack_require__(362);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__analytics_analytics__ = __webpack_require__(349);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4_ionic_angular__ = __webpack_require__(20);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};





var TabsPage = /** @class */ (function () {
    function TabsPage(navparams) {
        this.navparams = navparams;
        this.tab1Root = __WEBPACK_IMPORTED_MODULE_1__booking_bookingList__["a" /* BookingListPage */];
        this.tab2Root = __WEBPACK_IMPORTED_MODULE_3__analytics_analytics__["a" /* AnalyticsPage */];
        this.tab3Root = __WEBPACK_IMPORTED_MODULE_2__contracts_contracts_component__["a" /* ContractsComponent */];
        this.navParams = navparams.data;
    }
    TabsPage = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["m" /* Component */])({template:/*ion-inline-start:"/Users/alexg/git/multibanking/multibanking-app/src/pages/tabs/tabs.html"*/'<ion-tabs>\n  <ion-tab [root]="tab1Root" [rootParams]="navParams" tabTitle="Bookings" tabIcon="cash"></ion-tab>\n  <ion-tab [root]="tab2Root" [rootParams]="navParams" tabTitle="Analytics" tabIcon="analytics"></ion-tab>\n  <ion-tab [root]="tab3Root" [rootParams]="navParams" tabTitle="Contracts" tabIcon="contract"></ion-tab>\n</ion-tabs>\n'/*ion-inline-end:"/Users/alexg/git/multibanking/multibanking-app/src/pages/tabs/tabs.html"*/
        }),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_4_ionic_angular__["g" /* NavParams */]])
    ], TabsPage);
    return TabsPage;
}());

//# sourceMappingURL=tabs.js.map

/***/ }),

/***/ 356:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return BookingListPage; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_ionic_angular__ = __webpack_require__(20);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__services_bankAccountService__ = __webpack_require__(62);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__services_bookingService__ = __webpack_require__(357);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__services_LogoService__ = __webpack_require__(152);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__payment_paymentCreate__ = __webpack_require__(358);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6__booking_detail_bookingDetail__ = __webpack_require__(360);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};







var BookingListPage = /** @class */ (function () {
    function BookingListPage(navCtrl, navparams, alertCtrl, toastCtrl, loadingCtrl, bankAccountService, bookingService, logoService) {
        this.navCtrl = navCtrl;
        this.navparams = navparams;
        this.alertCtrl = alertCtrl;
        this.toastCtrl = toastCtrl;
        this.loadingCtrl = loadingCtrl;
        this.bankAccountService = bankAccountService;
        this.bookingService = bookingService;
        this.logoService = logoService;
        this.bankAccess = navparams.data.bankAccess;
        this.bankAccount = navparams.data.bankAccount;
        this.getLogo = logoService.getLogo;
    }
    BookingListPage.prototype.ngOnInit = function () {
        var _this = this;
        this.bookingService.getBookings(this.bankAccess.id, this.bankAccount.id).subscribe(function (response) {
            _this.bookings = response;
        }, function (error) {
            if (error == "SYNC_IN_PROGRESS") {
                _this.toastCtrl.create({
                    message: 'Account sync in progress',
                    showCloseButton: true,
                    position: 'top'
                }).present();
            }
        });
    };
    BookingListPage.prototype.ionViewDidLoad = function () {
        var _this = this;
        this.navBar.backButtonClick = function (e) {
            _this.navCtrl.parent.viewCtrl.dismiss();
        };
    };
    BookingListPage.prototype.syncBookingsPromptPin = function () {
        var _this = this;
        var alert = this.alertCtrl.create({
            title: 'Pin',
            inputs: [
                {
                    name: 'pin',
                    placeholder: 'Bank Account Pin',
                    type: 'password'
                }
            ],
            buttons: [
                {
                    text: 'Cancel',
                    role: 'cancel'
                },
                {
                    text: 'Submit',
                    handler: function (data) {
                        if (data.pin.length > 0) {
                            _this.syncBookings(data.pin);
                        }
                    }
                }
            ]
        });
        alert.present();
    };
    BookingListPage.prototype.syncBookings = function (pin) {
        var _this = this;
        if (!pin && !this.bankAccess.storePin) {
            return this.syncBookingsPromptPin();
        }
        var loading = this.loadingCtrl.create({
            content: 'Please wait...'
        });
        loading.present();
        this.bankAccountService.syncBookings(this.bankAccess.id, this.bankAccount.id, pin).subscribe(function (response) {
            _this.bookings = response;
            loading.dismiss();
        }, function (error) {
            if (error && error.messages) {
                error.messages.forEach(function (message) {
                    if (message.key == "SYNC_IN_PROGRESS") {
                        _this.toastCtrl.create({
                            message: 'Account sync in progress',
                            showCloseButton: true,
                            position: 'top'
                        }).present();
                    }
                    else if (message.key == "INVALID_PIN") {
                        _this.alertCtrl.create({
                            message: 'Invalid pin',
                            buttons: ['OK']
                        }).present();
                    }
                });
            }
        });
    };
    BookingListPage.prototype.itemSelected = function (booking) {
        this.navCtrl.push(__WEBPACK_IMPORTED_MODULE_6__booking_detail_bookingDetail__["a" /* BookingDetailPage */], {
            booking: booking
        });
    };
    BookingListPage.prototype.createPayment = function () {
        this.navCtrl.push(__WEBPACK_IMPORTED_MODULE_5__payment_paymentCreate__["a" /* PaymentCreatePage */], {
            bankAccount: this.bankAccount,
            bankAccess: this.bankAccess
        });
    };
    __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["_8" /* ViewChild */])(__WEBPACK_IMPORTED_MODULE_1_ionic_angular__["h" /* Navbar */]),
        __metadata("design:type", __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["h" /* Navbar */])
    ], BookingListPage.prototype, "navBar", void 0);
    BookingListPage = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["m" /* Component */])({
            selector: 'page-bookingList',template:/*ion-inline-start:"/Users/alexg/git/multibanking/multibanking-app/src/pages/booking/bookingList.html"*/'<ion-header>\n  <ion-navbar class="force-back-button">\n    <ion-title>Bookings</ion-title>\n    <ion-buttons end>\n      <button ion-button (click)="syncBookings()">\n        Reload\n      </button>\n    </ion-buttons>\n  </ion-navbar>\n</ion-header>\n\n<ion-content class="bookingList-content" padding>\n  <ion-fab bottom right>\n    <button ion-fab mini (click)="createPayment()">\n      <ion-icon name="paper-plane"></ion-icon>\n    </button>\n  </ion-fab>\n  <ion-list>\n    <ion-item *ngFor="let booking of bookings" (click)="itemSelected(booking)" [ngStyle]="{\'background-color\':!booking?.bookingCategory?.mainCategory ? \'chocolate\' : \'\' }">\n      <ion-thumbnail item-left>\n        <img [src]="getLogo(booking?.bookingCategory?.contract?.logo)" item-left/>\n      </ion-thumbnail>\n      <h3>{{ booking.otherAccount !=null ? booking.otherAccount.owner : ""}}</h3>\n      <h4 *ngIf="booking.bookingCategory">{{ booking.bookingCategory.mainCategory}}</h4>\n      <h4 *ngIf="booking.bookingCategory">{{ booking.bookingCategory.subCategory}}</h4>\n      <h4 *ngIf="booking.bookingCategory">{{ booking.bookingCategory.specification}}</h4>\n      <div item-right>\n        <h4>{{ booking.bookingDate | date }}</h4>\n        <h2>{{ booking.amount | currency:\'EUR\':true }}</h2>\n      </div>\n    </ion-item>\n  </ion-list>\n</ion-content>\n'/*ion-inline-end:"/Users/alexg/git/multibanking/multibanking-app/src/pages/booking/bookingList.html"*/
        }),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_1_ionic_angular__["f" /* NavController */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["g" /* NavParams */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["a" /* AlertController */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["j" /* ToastController */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["e" /* LoadingController */],
            __WEBPACK_IMPORTED_MODULE_2__services_bankAccountService__["a" /* BankAccountService */],
            __WEBPACK_IMPORTED_MODULE_3__services_bookingService__["a" /* BookingService */],
            __WEBPACK_IMPORTED_MODULE_4__services_LogoService__["a" /* LogoService */]])
    ], BookingListPage);
    return BookingListPage;
}());

//# sourceMappingURL=bookingList.js.map

/***/ }),

/***/ 357:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return BookingService; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__app_app_config__ = __webpack_require__(23);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__angular_http__ = __webpack_require__(33);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};




var BookingService = /** @class */ (function () {
    function BookingService(http) {
        this.http = http;
    }
    BookingService.prototype.getBookings = function (accessId, accountId) {
        return this.http.get(__WEBPACK_IMPORTED_MODULE_1__app_app_config__["a" /* AppConfig */].api_url + "/bankaccesses/" + accessId + "/accounts/" + accountId + "/bookings")
            .map(function (res) { return res.json()._embedded != null ? res.json()._embedded.bookingEntityList : []; })
            .catch(this.handleError);
    };
    BookingService.prototype.getBooking = function (accessId, accountId, bookingId) {
        return this.http.get(__WEBPACK_IMPORTED_MODULE_1__app_app_config__["a" /* AppConfig */].api_url + "/bankaccesses/" + accessId + "/accounts/" + accountId + "/bookings/" + bookingId)
            .map(function (res) { return res.json()._embedded != null ? res.json()._embedded.bookingEntityList : []; })
            .catch(this.handleError);
    };
    BookingService.prototype.handleError = function (error) {
        console.error(error);
        var errorJson = error.json();
        if (errorJson) {
            if (errorJson.message == "SYNC_IN_PROGRESS") {
                return __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__["Observable"].throw(errorJson.message);
            }
            else {
                return __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__["Observable"].throw(errorJson || 'Server error');
            }
        }
        else {
            return __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__["Observable"].throw(error || 'Server error');
        }
    };
    BookingService = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["A" /* Injectable */])(),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_2__angular_http__["c" /* Http */]])
    ], BookingService);
    return BookingService;
}());

//# sourceMappingURL=bookingService.js.map

/***/ }),

/***/ 358:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return PaymentCreatePage; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_ionic_angular__ = __webpack_require__(20);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__services_PaymentService__ = __webpack_require__(359);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};



var PaymentCreatePage = /** @class */ (function () {
    function PaymentCreatePage(navCtrl, navparams, loadingCtrl, toastCtrl, alertCtrl, paymentService) {
        this.navCtrl = navCtrl;
        this.navparams = navparams;
        this.loadingCtrl = loadingCtrl;
        this.toastCtrl = toastCtrl;
        this.alertCtrl = alertCtrl;
        this.paymentService = paymentService;
        this.payment = { receiver: "", purpose: "", amount: undefined };
        this.bankAccount = navparams.data.bankAccount;
        this.bankAccess = navparams.data.bankAccess;
    }
    PaymentCreatePage.prototype.ngOnInit = function () {
    };
    PaymentCreatePage.prototype.createPayment = function (pin) {
        var _this = this;
        if (!pin && !this.bankAccess.storePin) {
            return this.createPaymentPromptPin();
        }
        var loading = this.loadingCtrl.create({
            content: 'Please wait...'
        });
        loading.present();
        this.paymentService.createPayment(this.bankAccess.id, this.bankAccount.id, { payment: this.payment, pin: pin }).subscribe(function (paymentLocation) {
            loading.dismiss();
            _this.askForTan(paymentLocation);
        }, function (error) {
            if (error && error.messages) {
                error.messages.forEach(function (message) {
                    if (message.key == "ERROR_PAYMENT") {
                        _this.alertCtrl.create({
                            message: 'Payment Error',
                            buttons: ['OK']
                        }).present();
                    }
                    else if (message.key == "INVALID_PIN") {
                        _this.alertCtrl.create({
                            message: 'Invalid pin',
                            buttons: ['OK']
                        }).present();
                    }
                });
            }
        });
    };
    PaymentCreatePage.prototype.createPaymentPromptPin = function () {
        var _this = this;
        var alert = this.alertCtrl.create({
            title: 'Pin',
            inputs: [
                {
                    name: 'pin',
                    placeholder: 'Bank Account Pin',
                    type: 'password'
                }
            ],
            buttons: [
                {
                    text: 'Cancel',
                    role: 'cancel'
                },
                {
                    text: 'Submit',
                    handler: function (data) {
                        if (data.pin.length > 0) {
                            _this.createPayment(data.pin);
                        }
                    }
                }
            ]
        });
        alert.present();
    };
    PaymentCreatePage.prototype.askForTan = function (paymentLocation) {
        var _this = this;
        this.paymentService.getPayment(paymentLocation).subscribe(function (payment) {
            var alert = _this.alertCtrl.create({
                title: payment.paymentChallenge.title,
                inputs: [
                    {
                        name: 'tan',
                        placeholder: 'Tan',
                        type: 'text'
                    }
                ],
                buttons: [
                    {
                        text: 'Cancel',
                        role: 'cancel'
                    },
                    {
                        text: 'Submit',
                        handler: function (data) {
                            if (data.tan.length > 0) {
                                _this.submitPayment(payment, data.tan);
                            }
                        }
                    }
                ]
            });
            alert.present();
        });
    };
    PaymentCreatePage.prototype.submitPayment = function (payment, tan) {
        var _this = this;
        this.paymentService.submitPayment(this.bankAccess.id, this.bankAccount.id, payment.id, { tan: tan }).subscribe(function (response) {
            _this.toastCtrl.create({
                message: 'Payment successful',
                showCloseButton: true,
                position: 'top'
            }).present();
        });
    };
    PaymentCreatePage = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["m" /* Component */])({
            selector: 'page-payment-create',template:/*ion-inline-start:"/Users/alexg/git/multibanking/multibanking-app/src/pages/payment/paymentCreate.html"*/'<ion-header>\n  <ion-navbar>\n    <ion-title>New SEPA Transfer</ion-title>\n  </ion-navbar>\n</ion-header>\n\n<ion-content class="payment-create-content" padding>\n\n  <form (ngSubmit)="createPayment()" #createForm="ngForm">\n    <ion-item-group>\n        <ion-item-divider color="light">Debit Account {{bankAccount.bankName}} {{bankAccount.accountNumber}}</ion-item-divider>\n      <ion-item>\n        <ion-label floating>Receiver Name</ion-label>\n        <ion-input type="text" name="receiver" [(ngModel)]="payment.receiver" required></ion-input>\n      </ion-item>\n      <ion-item>\n        <ion-label floating>Receiver IBAN</ion-label>\n        <ion-input type="text" name="receiverIban" [(ngModel)]="payment.receiverIban" required></ion-input>\n      </ion-item>\n      <ion-item>\n        <ion-label floating>Amount</ion-label>\n        <ion-input type="number" name="amount" [(ngModel)]="payment.amount" required></ion-input>\n      </ion-item>\n      <ion-item>\n        <ion-label floating>Purpose</ion-label>\n        <ion-input type="text" name="purpose" [(ngModel)]="payment.purpose"></ion-input>\n      </ion-item>\n    </ion-item-group>\n    <button ion-button color="primary" block type="submit" [disabled]="!createForm.form.valid">Submit</button>\n  </form>\n</ion-content>\n'/*ion-inline-end:"/Users/alexg/git/multibanking/multibanking-app/src/pages/payment/paymentCreate.html"*/
        }),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_1_ionic_angular__["f" /* NavController */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["g" /* NavParams */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["e" /* LoadingController */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["j" /* ToastController */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["a" /* AlertController */],
            __WEBPACK_IMPORTED_MODULE_2__services_PaymentService__["a" /* PaymentService */]])
    ], PaymentCreatePage);
    return PaymentCreatePage;
}());

//# sourceMappingURL=paymentCreate.js.map

/***/ }),

/***/ 359:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return PaymentService; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__app_app_config__ = __webpack_require__(23);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__angular_http__ = __webpack_require__(33);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};




var PaymentService = /** @class */ (function () {
    function PaymentService(http) {
        this.http = http;
    }
    PaymentService.prototype.getPayment = function (location) {
        return this.http.get(location)
            .map(function (res) { return res.json(); })
            .catch(this.handleError);
    };
    PaymentService.prototype.createPayment = function (accessId, accountId, paymentCreate) {
        return this.http.post(__WEBPACK_IMPORTED_MODULE_1__app_app_config__["a" /* AppConfig */].api_url + "/bankaccesses/" + accessId + "/accounts/" + accountId + "/payments", paymentCreate)
            .map(function (res) { return res.headers.get("Location"); })
            .catch(this.handleError);
    };
    PaymentService.prototype.submitPayment = function (accessId, accountId, paymentId, paymentSubmit) {
        return this.http.post(__WEBPACK_IMPORTED_MODULE_1__app_app_config__["a" /* AppConfig */].api_url + "/bankaccesses/" + accessId + "/accounts/" + accountId + "/payments/" + paymentId + "/submit", paymentSubmit)
            .catch(this.handleError);
    };
    PaymentService.prototype.handleError = function (error) {
        console.error(error);
        var errorJson = error.json();
        if (errorJson) {
            return __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__["Observable"].throw(errorJson || 'Server error');
        }
        else {
            return __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__["Observable"].throw(error || 'Server error');
        }
    };
    PaymentService = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["A" /* Injectable */])(),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_2__angular_http__["c" /* Http */]])
    ], PaymentService);
    return PaymentService;
}());

//# sourceMappingURL=PaymentService.js.map

/***/ }),

/***/ 360:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return BookingDetailPage; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_ionic_angular__ = __webpack_require__(20);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__booking_edit_bookingEdit__ = __webpack_require__(361);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};



var BookingDetailPage = /** @class */ (function () {
    function BookingDetailPage(navCtrl, navparams) {
        this.navCtrl = navCtrl;
        this.navparams = navparams;
        this.booking = navparams.data.booking;
    }
    BookingDetailPage.prototype.editCategory = function () {
        this.navCtrl.push(__WEBPACK_IMPORTED_MODULE_2__booking_edit_bookingEdit__["a" /* BookingEditPage */], {
            booking: this.booking
        });
    };
    BookingDetailPage = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["m" /* Component */])({
            selector: 'page-bookingDetail',template:/*ion-inline-start:"/Users/alexg/git/multibanking/multibanking-app/src/pages/booking-detail/bookingDetail.html"*/'<ion-header>\n  <ion-navbar class="force-back-button">\n    <ion-title>{{ booking.bookingDate | date }} {{ booking.amount | currency:\'EUR\':true }}</ion-title>\n  </ion-navbar>\n</ion-header>\n\n<ion-content class="bookingDetail-content" padding>\n  <ion-fab bottom right>\n    <button ion-fab mini (click)="editCategory()">\n      <ion-icon name="create"></ion-icon>\n    </button>\n  </ion-fab>\n  <ion-grid>\n    <ion-item>\n      <ion-row>\n        <ion-col col-12 col-lg-6>\n          <ion-row>\n            <ion-col col-4>Booking date:</ion-col>\n            <ion-col col-8>{{ booking.bookingDate | date }}</ion-col>\n            <ion-col col-4>Amount:</ion-col>\n            <ion-col col-8>{{ booking.amount | currency:\'EUR\':true }}</ion-col>\n            <ion-col col-4 *ngIf="booking.otherAccount">Receiver:</ion-col>\n            <ion-col col-8 *ngIf="booking.otherAccount">{{ booking.otherAccount.owner }}</ion-col>\n            <ion-col col-4>Standing order:</ion-col>\n            <ion-col col-8>{{ booking.standingOrder }}</ion-col>\n          </ion-row>\n        </ion-col>\n        <ion-col col-12 col-lg-6>\n          <ion-row>\n            <ion-col col-4 *ngIf="booking.creditorId">Creditor ID:</ion-col>\n            <ion-col col-8 *ngIf="booking.creditorId">{{ booking.creditorId }}</ion-col>\n            <ion-col col-4 *ngIf="booking.mandateReference">Mandate reference:</ion-col>\n            <ion-col col-8 *ngIf="booking.mandateReference">{{ booking.mandateReference }}</ion-col>\n            <ion-col col-4>Bank API:</ion-col>\n            <ion-col col-8>{{ booking.bankApi }}</ion-col>\n          </ion-row>\n        </ion-col>\n        <ion-col col-12>\n          <ion-row>\n            <ion-col col-2>Purpose:</ion-col>\n            <ion-col col-10><p style="white-space: normal;">{{ booking.usage }}</p></ion-col>\n          </ion-row>\n        </ion-col>\n      </ion-row>\n    </ion-item>\n    <ion-item>\n      <ion-row>\n        <ion-col col-12 col-lg-6>\n          <ion-row>\n            <ion-col col-4>Rules:</ion-col>\n            <ion-col col-8>{{ booking.bookingCategory?.rules }}</ion-col>\n            <ion-col col-4>Main category:</ion-col>\n            <ion-col col-8>{{ booking.bookingCategory?.mainCategory }}</ion-col>\n            <ion-col col-4>Subcategory:</ion-col>\n            <ion-col col-8>{{ booking.bookingCategory?.subCategory }}</ion-col>\n            <ion-col col-4>Specification:</ion-col>\n            <ion-col col-8>{{ booking.bookingCategory?.specification }}</ion-col>\n          </ion-row>\n        </ion-col>\n      </ion-row>\n    </ion-item>\n  </ion-grid>\n</ion-content>\n'/*ion-inline-end:"/Users/alexg/git/multibanking/multibanking-app/src/pages/booking-detail/bookingDetail.html"*/
        }),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_1_ionic_angular__["f" /* NavController */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["g" /* NavParams */]])
    ], BookingDetailPage);
    return BookingDetailPage;
}());

//# sourceMappingURL=bookingDetail.js.map

/***/ }),

/***/ 361:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return BookingEditPage; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_ionic_angular__ = __webpack_require__(20);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__services_analyticsService__ = __webpack_require__(151);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};



var BookingEditPage = /** @class */ (function () {
    function BookingEditPage(navCtrl, navparams, analyticsService) {
        this.navCtrl = navCtrl;
        this.analyticsService = analyticsService;
        this.booking = navparams.data.booking;
    }
    BookingEditPage.prototype.ngOnInit = function () {
        var _this = this;
        this.analyticsService.getAvailableCategories().subscribe(function (response) {
            _this.categories = response;
            if (_this.booking.bookingCategory && _this.booking.bookingCategory.mainCategory) {
                _this.mainCategoryChanged(_this.booking.bookingCategory.mainCategory);
                if (_this.booking.bookingCategory.subCategory) {
                    _this.subCategoryChanged(_this.booking.bookingCategory.subCategory);
                }
                if (_this.booking.bookingCategory.specification) {
                    _this.specificationChanged(_this.booking.bookingCategory.specification);
                }
            }
        });
    };
    BookingEditPage.prototype.mainCategoryChanged = function (catId) {
        this.mainCategoryId = catId;
        this.mainCategory = this.categories.filter(function (element) { return element.id == catId; })[0];
        this.subCategories = this.mainCategory.subcategories;
    };
    BookingEditPage.prototype.subCategoryChanged = function (catId) {
        this.subCategoryId = catId;
        this.subCategory = this.subCategories.filter(function (element) { return element.id == catId; })[0];
        this.specifications = this.subCategory.specifications;
    };
    BookingEditPage.prototype.specificationChanged = function (catId) {
        this.specificationId = catId;
        this.specification = this.specifications.filter(function (element) { return element.id == catId; })[0];
    };
    BookingEditPage.prototype.submit = function () {
        var _this = this;
        var rule = {
            ruleId: this.booking.bookingCategory ? this.booking.bookingCategory.rules.reverse()[0] : null,
            creditorId: this.booking.creditorId,
            receiver: this.receiver,
            expression: this.expression,
            mainCategory: this.mainCategory.id,
            subCategory: this.subCategory ? this.subCategory.id : null,
            specification: this.specification ? this.specification.id : null,
            incoming: this.booking.amount > 0,
            taxRelevant: this.taxRelevant
        };
        this.analyticsService.createRule(rule).subscribe(function (response) {
            _this.navCtrl.pop();
        });
    };
    BookingEditPage = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["m" /* Component */])({
            selector: 'page-bookingEdit',template:/*ion-inline-start:"/Users/alexg/git/multibanking/multibanking-app/src/pages/booking-edit/bookingEdit.html"*/'<ion-header>\n  <ion-navbar class="force-back-button">\n    <ion-title>Categorize Booking</ion-title>\n  </ion-navbar>\n</ion-header>\n\n<ion-content class="bookingEdit-content" padding>\n  <ion-list>\n    <ion-item>\n      <ion-label>Receiver</ion-label>\n      <ion-input type="text" name="receiver" [(ngModel)]="receiver"></ion-input>\n    </ion-item>\n    <ion-item>\n      <ion-label>Expression</ion-label>\n      <ion-input type="text" name="expression" [(ngModel)]="expression"></ion-input>\n    </ion-item>\n    <ion-item>\n      <ion-label>Main category</ion-label>\n      <ion-select [(ngModel)]="mainCategoryId" (ionChange)="mainCategoryChanged($event)">\n        <ion-option *ngFor="let category of categories" value="{{category.id}}">{{category.name}}</ion-option>\n      </ion-select>\n    </ion-item>\n    <ion-item>\n      <ion-label>Subcategory</ion-label>\n      <ion-select [(ngModel)]="subCategoryId" (ionChange)="subCategoryChanged($event)">\n        <ion-option *ngFor="let category of mainCategory?.subcategories" value="{{category.id}}">{{category.name}}</ion-option>\n      </ion-select>\n    </ion-item>\n    <ion-item>\n      <ion-label>Specification</ion-label>\n      <ion-select [(ngModel)]="specificationId" (ionChange)="specificationChanged($event)">\n        <ion-option *ngFor="let category of subCategory?.specifications" value="{{category.id}}">{{category.name}}</ion-option>\n      </ion-select>\n    </ion-item>\n    <ion-item>\n      <ion-label>Tax relevant</ion-label>\n      <ion-toggle [(ngModel)]="taxRelevant"></ion-toggle>\n    </ion-item>\n  </ion-list>\n  <button block icon-start ion-button color="primary" type="submit" (click)="submit()" [disabled]="!mainCategory || (!expression && !booking.creditorId)">\n    Speichern\n  </button>\n</ion-content>\n'/*ion-inline-end:"/Users/alexg/git/multibanking/multibanking-app/src/pages/booking-edit/bookingEdit.html"*/
        }),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_1_ionic_angular__["f" /* NavController */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["g" /* NavParams */],
            __WEBPACK_IMPORTED_MODULE_2__services_analyticsService__["a" /* AnalyticsService */]])
    ], BookingEditPage);
    return BookingEditPage;
}());

//# sourceMappingURL=bookingEdit.js.map

/***/ }),

/***/ 362:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return ContractsComponent; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_ionic_angular__ = __webpack_require__(20);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__contract_service__ = __webpack_require__(363);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__services_LogoService__ = __webpack_require__(152);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__services_bankAccountService__ = __webpack_require__(62);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};





var ContractsComponent = /** @class */ (function () {
    function ContractsComponent(navCtrl, navParams, alertCtrl, toastCtrl, loadingCtrl, contractService, bankAccountService, logoService) {
        this.navCtrl = navCtrl;
        this.navParams = navParams;
        this.alertCtrl = alertCtrl;
        this.toastCtrl = toastCtrl;
        this.loadingCtrl = loadingCtrl;
        this.contractService = contractService;
        this.bankAccountService = bankAccountService;
        this.logoService = logoService;
        this.bankAccess = navParams.data.bankAccess;
        this.bankAccountId = navParams.data.bankAccount.id;
        this.getLogo = logoService.getLogo;
    }
    ContractsComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.bankAccountService.bookingsChangedObservable.subscribe(function (changed) {
            _this.loadContracts();
        });
        this.loadContracts();
    };
    ContractsComponent.prototype.ionViewDidLoad = function () {
        var _this = this;
        this.navBar.backButtonClick = function (e) {
            _this.navCtrl.parent.viewCtrl.dismiss();
        };
    };
    ContractsComponent.prototype.loadContracts = function () {
        var _this = this;
        this.contracts = {
            income: [],
            expenses: []
        };
        this.contractService.getContracts(this.bankAccess.id, this.bankAccountId)
            .subscribe(function (contracts) {
            contracts.reduce(function (acc, contract) {
                contract.amount > 0 ? acc.income.push(contract) : acc.expenses.push(contract);
                return acc;
            }, _this.contracts);
        });
    };
    ContractsComponent.prototype.syncBookingsPromptPin = function () {
        var _this = this;
        var alert = this.alertCtrl.create({
            title: 'Pin',
            inputs: [
                {
                    name: 'pin',
                    placeholder: 'Bank Account Pin',
                    type: 'password'
                }
            ],
            buttons: [
                {
                    text: 'Cancel',
                    role: 'cancel'
                },
                {
                    text: 'Submit',
                    handler: function (data) {
                        if (data.pin.length > 0) {
                            _this.syncBookings(data.pin);
                        }
                    }
                }
            ]
        });
        alert.present();
    };
    ContractsComponent.prototype.syncBookings = function (pin) {
        var _this = this;
        if (!pin && !this.bankAccess.storePin) {
            return this.syncBookingsPromptPin();
        }
        var loading = this.loadingCtrl.create({
            content: 'Please wait...'
        });
        loading.present();
        this.bankAccountService.syncBookings(this.bankAccess.id, this.bankAccountId, pin).subscribe(function (response) {
            loading.dismiss();
        }, function (error) {
            if (error && error.messages) {
                error.messages.forEach(function (message) {
                    if (message.key == "SYNC_IN_PROGRESS") {
                        _this.toastCtrl.create({
                            message: 'Account sync in progress',
                            showCloseButton: true,
                            position: 'top'
                        }).present();
                    }
                    else if (message.key == "INVALID_PIN") {
                        _this.alertCtrl.create({
                            message: 'Invalid pin',
                            buttons: ['OK']
                        }).present();
                    }
                });
            }
        });
    };
    __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["_8" /* ViewChild */])(__WEBPACK_IMPORTED_MODULE_1_ionic_angular__["h" /* Navbar */]),
        __metadata("design:type", __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["h" /* Navbar */])
    ], ContractsComponent.prototype, "navBar", void 0);
    ContractsComponent = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["m" /* Component */])({
            selector: 'contracts',template:/*ion-inline-start:"/Users/alexg/git/multibanking/multibanking-app/src/pages/contracts/contracts.component.html"*/'<ion-header>\n  <ion-navbar class="force-back-button">\n    <ion-title>Contracts</ion-title>\n    <ion-buttons end>\n      <button ion-button (click)="syncBookings()">\n        Reload\n      </button>\n    </ion-buttons>\n  </ion-navbar>\n</ion-header>\n\n<ion-content class="contracts" padding>\n  <ion-list>\n    <h3 class="type">Income</h3>\n    <ion-item *ngFor="let contract of contracts.income">\n      <ion-thumbnail item-left>\n        <img *ngIf="contract.logo" [src]="getLogo(contract.logo)" item-left/>\n      </ion-thumbnail>\n      <h4 *ngIf="contract.provider">{{contract.provider}}</h4>\n      <h4 *ngIf="contract.mainCategory">{{contract.mainCategory}}</h4>\n      <h4 *ngIf="contract.subCategory">{{contract.subCategory}}</h4>\n      <h4 *ngIf="contract.specification">{{contract.specification}}</h4>\n      <div item-right>\n        <h2>{{contract.amount | currency:\'EUR\':true}} {{contract.cycle | cycle}}</h2>\n      </div>\n    </ion-item>\n\n    <h3 class="type">Expenses</h3>\n    <ion-item *ngFor="let contract of contracts.expenses">\n        <ion-thumbnail item-left>\n          <img *ngIf="contract.logo" [src]="getLogo(contract.logo)" item-left/>\n        </ion-thumbnail>\n        <h4 *ngIf="contract.provider">{{contract.provider}}</h4>\n        <h4 *ngIf="contract.mainCategory">{{contract.mainCategory}}</h4>\n        <h4 *ngIf="contract.subCategory">{{contract.subCategory}}</h4>\n        <h4 *ngIf="contract.specification">{{contract.specification}}</h4>\n        <div item-right>\n          <h2>{{contract.amount | currency:\'EUR\':true}} {{contract.cycle | cycle}}</h2>\n        </div>\n      </ion-item>\n  </ion-list>\n</ion-content>\n'/*ion-inline-end:"/Users/alexg/git/multibanking/multibanking-app/src/pages/contracts/contracts.component.html"*/
        }),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_1_ionic_angular__["f" /* NavController */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["g" /* NavParams */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["a" /* AlertController */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["j" /* ToastController */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["e" /* LoadingController */],
            __WEBPACK_IMPORTED_MODULE_2__contract_service__["a" /* ContractService */],
            __WEBPACK_IMPORTED_MODULE_4__services_bankAccountService__["a" /* BankAccountService */],
            __WEBPACK_IMPORTED_MODULE_3__services_LogoService__["a" /* LogoService */]])
    ], ContractsComponent);
    return ContractsComponent;
}());

//# sourceMappingURL=contracts.component.js.map

/***/ }),

/***/ 363:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return ContractService; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__app_app_config__ = __webpack_require__(23);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__angular_http__ = __webpack_require__(33);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};




var ContractService = /** @class */ (function () {
    function ContractService(http) {
        this.http = http;
    }
    ContractService.prototype.getContracts = function (accessId, accountId) {
        return this.http.get(__WEBPACK_IMPORTED_MODULE_1__app_app_config__["a" /* AppConfig */].api_url + "/bankaccesses/" + accessId + "/accounts/" + accountId + "/contracts")
            .map(function (res) { return res.json()._embedded.contractEntityList; })
            .catch(this.handleError);
    };
    ContractService.prototype.handleError = function (error) {
        var errorJson = error.json();
        if (errorJson) {
            if (errorJson.message == "SYNC_IN_PROGRESS") {
                return __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__["Observable"].throw(errorJson.message);
            }
            else {
                return __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__["Observable"].throw(errorJson || 'Server error');
            }
        }
        else {
            return __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__["Observable"].throw(error || 'Server error');
        }
    };
    ContractService = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["A" /* Injectable */])(),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_2__angular_http__["c" /* Http */]])
    ], ContractService);
    return ContractService;
}());

//# sourceMappingURL=contract.service.js.map

/***/ }),

/***/ 364:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return BankAccessUpdatePage; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_ionic_angular__ = __webpack_require__(20);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__services_bankAccessService__ = __webpack_require__(86);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};



var BankAccessUpdatePage = /** @class */ (function () {
    function BankAccessUpdatePage(navCtrl, navparams, loadingCtrl, bankAccessService) {
        this.navCtrl = navCtrl;
        this.navparams = navparams;
        this.loadingCtrl = loadingCtrl;
        this.bankAccessService = bankAccessService;
        this.bankAccess = navparams.data.bankAccess;
        this.parent = navparams.data.parent;
    }
    BankAccessUpdatePage.prototype.updateBankAccess = function () {
        var _this = this;
        var loading = this.loadingCtrl.create({
            content: 'Please wait...'
        });
        loading.present();
        this.bankAccessService.updateBankAcccess(this.bankAccess).subscribe(function (response) {
            loading.dismiss();
            _this.parent.bankAccessesChanged();
            _this.navCtrl.pop();
        });
    };
    BankAccessUpdatePage = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["m" /* Component */])({
            selector: 'page-bankaccess-update',template:/*ion-inline-start:"/Users/alexg/git/multibanking/multibanking-app/src/pages/bankaccess/bankAccessUpdate.html"*/'<ion-header>\n  <ion-navbar>\n    <ion-title>Edit bank connection {{bankAccess.bankName}}</ion-title>\n  </ion-navbar>\n</ion-header>\n\n<ion-content class="bankaccess-create-content" padding>\n  <form (ngSubmit)="updateBankAccess()" #createForm="ngForm">\n    <ion-item-group>\n      <ion-item-divider color="light">Bank login data</ion-item-divider>\n      <ion-item *ngIf="bankAccess.storePin">>\n        <ion-label floating>Pin</ion-label>\n        <ion-input type="password" name="pin" [(ngModel)]="bankAccess.pin"></ion-input>\n      </ion-item>\n    </ion-item-group>\n    <ion-item-group>\n      <ion-item-divider color="light">Bank settings</ion-item-divider>\n      <ion-item>\n        <ion-label>Save Pin</ion-label>\n        <ion-toggle [(ngModel)]="bankAccess.storePin" [ngModelOptions]="{standalone: true}"></ion-toggle>\n      </ion-item>\n\n      <ion-item>\n        <ion-label>Save Bookings</ion-label>\n        <ion-toggle [(ngModel)]="bankAccess.storeBookings" [ngModelOptions]="{standalone: true}"></ion-toggle>\n      </ion-item>\n\n      <ion-item>\n        <ion-label>Categorize Bookings</ion-label>\n        <ion-toggle [(ngModel)]="bankAccess.categorizeBookings" [ngModelOptions]="{standalone: true}"></ion-toggle>\n      </ion-item>\n\n      <ion-item>\n        <ion-label>Save anonymized Bookings</ion-label>\n        <ion-toggle [(ngModel)]="bankAccess.storeAnonymizedBookings" [ngModelOptions]="{standalone: true}"></ion-toggle>\n      </ion-item>\n\n      <ion-item>\n        <ion-label>Save Analytics</ion-label>\n        <ion-toggle [(ngModel)]="bankAccess.storeAnalytics" [ngModelOptions]="{standalone: true}"></ion-toggle>\n      </ion-item>\n    </ion-item-group>\n\n    <button ion-button color="primary" block type="submit">Submit</button>\n\n  </form>\n</ion-content>\n'/*ion-inline-end:"/Users/alexg/git/multibanking/multibanking-app/src/pages/bankaccess/bankAccessUpdate.html"*/
        }),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_1_ionic_angular__["f" /* NavController */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["g" /* NavParams */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["e" /* LoadingController */],
            __WEBPACK_IMPORTED_MODULE_2__services_bankAccessService__["a" /* BankAccessService */]])
    ], BankAccessUpdatePage);
    return BankAccessUpdatePage;
}());

//# sourceMappingURL=bankAccessUpdate.js.map

/***/ }),

/***/ 365:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
Object.defineProperty(__webpack_exports__, "__esModule", { value: true });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__angular_platform_browser_dynamic__ = __webpack_require__(368);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__app_module__ = __webpack_require__(370);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__app_config__ = __webpack_require__(23);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__auth_keycloak_service__ = __webpack_require__(153);





if (__WEBPACK_IMPORTED_MODULE_3__app_config__["a" /* AppConfig */].production) {
    Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["_14" /* enableProdMode */])();
}
__WEBPACK_IMPORTED_MODULE_4__auth_keycloak_service__["a" /* KeycloakService */].init()
    .then(function () { return Object(__WEBPACK_IMPORTED_MODULE_1__angular_platform_browser_dynamic__["a" /* platformBrowserDynamic */])().bootstrapModule(__WEBPACK_IMPORTED_MODULE_2__app_module__["a" /* AppModule */]); })
    .catch(function (e) {
    console.log(e);
    window.location.reload();
});
//# sourceMappingURL=main.js.map

/***/ }),

/***/ 370:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return AppModule; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0_ionic2_auto_complete__ = __webpack_require__(167);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__angular_platform_browser__ = __webpack_require__(42);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__angular_http__ = __webpack_require__(33);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4_ionic_angular__ = __webpack_require__(20);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__ionic_native_splash_screen__ = __webpack_require__(345);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6__ionic_native_status_bar__ = __webpack_require__(348);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_7__pages_analytics_analytics__ = __webpack_require__(349);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_8__services_analyticsService__ = __webpack_require__(151);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_9__pages_bankaccess_bankAccessCreate__ = __webpack_require__(351);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_10__pages_bankaccess_bankAccessList__ = __webpack_require__(353);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_11__services_bankAccessService__ = __webpack_require__(86);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_12__pages_bankaccess_bankAccessUpdate__ = __webpack_require__(364);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_13__pages_bankaccount_bankaccountList__ = __webpack_require__(354);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_14__services_bankAccountService__ = __webpack_require__(62);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_15__services_bankAutoCompleteService__ = __webpack_require__(352);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_16__pages_analytics_bookingGroup__ = __webpack_require__(350);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_17__pages_booking_bookingList__ = __webpack_require__(356);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_18__services_bookingService__ = __webpack_require__(357);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_19__pages_contracts_contract_service__ = __webpack_require__(363);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_20__pages_contracts_contracts_component__ = __webpack_require__(362);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_21__pages_contracts_cycle_pipe__ = __webpack_require__(693);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_22__auth_keycloak_http__ = __webpack_require__(694);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_23__auth_keycloak_service__ = __webpack_require__(153);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_24__services_LogoService__ = __webpack_require__(152);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_25__app_component__ = __webpack_require__(695);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_26__pages_tabs_tabs__ = __webpack_require__(355);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_27__pages_payment_paymentCreate__ = __webpack_require__(358);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_28__services_PaymentService__ = __webpack_require__(359);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_29__pages_booking_detail_bookingDetail__ = __webpack_require__(360);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_30__pages_booking_edit_bookingEdit__ = __webpack_require__(361);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};































var AppModule = /** @class */ (function () {
    function AppModule() {
    }
    AppModule = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_2__angular_core__["I" /* NgModule */])({
            declarations: [
                __WEBPACK_IMPORTED_MODULE_7__pages_analytics_analytics__["a" /* AnalyticsPage */],
                __WEBPACK_IMPORTED_MODULE_9__pages_bankaccess_bankAccessCreate__["a" /* BankAccessCreatePage */],
                __WEBPACK_IMPORTED_MODULE_10__pages_bankaccess_bankAccessList__["a" /* BankAccessListPage */],
                __WEBPACK_IMPORTED_MODULE_12__pages_bankaccess_bankAccessUpdate__["a" /* BankAccessUpdatePage */],
                __WEBPACK_IMPORTED_MODULE_13__pages_bankaccount_bankaccountList__["a" /* BankAccountListPage */],
                __WEBPACK_IMPORTED_MODULE_16__pages_analytics_bookingGroup__["a" /* BookingGroupPage */],
                __WEBPACK_IMPORTED_MODULE_17__pages_booking_bookingList__["a" /* BookingListPage */],
                __WEBPACK_IMPORTED_MODULE_29__pages_booking_detail_bookingDetail__["a" /* BookingDetailPage */],
                __WEBPACK_IMPORTED_MODULE_30__pages_booking_edit_bookingEdit__["a" /* BookingEditPage */],
                __WEBPACK_IMPORTED_MODULE_27__pages_payment_paymentCreate__["a" /* PaymentCreatePage */],
                __WEBPACK_IMPORTED_MODULE_20__pages_contracts_contracts_component__["a" /* ContractsComponent */],
                __WEBPACK_IMPORTED_MODULE_26__pages_tabs_tabs__["a" /* TabsPage */],
                __WEBPACK_IMPORTED_MODULE_21__pages_contracts_cycle_pipe__["a" /* CyclePipe */],
                __WEBPACK_IMPORTED_MODULE_25__app_component__["a" /* MyApp */],
            ],
            imports: [
                __WEBPACK_IMPORTED_MODULE_0_ionic2_auto_complete__["b" /* AutoCompleteModule */],
                __WEBPACK_IMPORTED_MODULE_1__angular_platform_browser__["a" /* BrowserModule */],
                __WEBPACK_IMPORTED_MODULE_3__angular_http__["d" /* HttpModule */],
                __WEBPACK_IMPORTED_MODULE_4_ionic_angular__["d" /* IonicModule */].forRoot(__WEBPACK_IMPORTED_MODULE_25__app_component__["a" /* MyApp */], {}, {
                    links: []
                }),
            ],
            bootstrap: [__WEBPACK_IMPORTED_MODULE_4_ionic_angular__["b" /* IonicApp */]],
            entryComponents: [
                __WEBPACK_IMPORTED_MODULE_7__pages_analytics_analytics__["a" /* AnalyticsPage */],
                __WEBPACK_IMPORTED_MODULE_9__pages_bankaccess_bankAccessCreate__["a" /* BankAccessCreatePage */],
                __WEBPACK_IMPORTED_MODULE_10__pages_bankaccess_bankAccessList__["a" /* BankAccessListPage */],
                __WEBPACK_IMPORTED_MODULE_12__pages_bankaccess_bankAccessUpdate__["a" /* BankAccessUpdatePage */],
                __WEBPACK_IMPORTED_MODULE_13__pages_bankaccount_bankaccountList__["a" /* BankAccountListPage */],
                __WEBPACK_IMPORTED_MODULE_16__pages_analytics_bookingGroup__["a" /* BookingGroupPage */],
                __WEBPACK_IMPORTED_MODULE_17__pages_booking_bookingList__["a" /* BookingListPage */],
                __WEBPACK_IMPORTED_MODULE_29__pages_booking_detail_bookingDetail__["a" /* BookingDetailPage */],
                __WEBPACK_IMPORTED_MODULE_30__pages_booking_edit_bookingEdit__["a" /* BookingEditPage */],
                __WEBPACK_IMPORTED_MODULE_27__pages_payment_paymentCreate__["a" /* PaymentCreatePage */],
                __WEBPACK_IMPORTED_MODULE_20__pages_contracts_contracts_component__["a" /* ContractsComponent */],
                __WEBPACK_IMPORTED_MODULE_26__pages_tabs_tabs__["a" /* TabsPage */],
                __WEBPACK_IMPORTED_MODULE_25__app_component__["a" /* MyApp */],
            ],
            providers: [
                __WEBPACK_IMPORTED_MODULE_8__services_analyticsService__["a" /* AnalyticsService */],
                __WEBPACK_IMPORTED_MODULE_11__services_bankAccessService__["a" /* BankAccessService */],
                __WEBPACK_IMPORTED_MODULE_14__services_bankAccountService__["a" /* BankAccountService */],
                __WEBPACK_IMPORTED_MODULE_15__services_bankAutoCompleteService__["a" /* BankAutoCompleteService */],
                __WEBPACK_IMPORTED_MODULE_18__services_bookingService__["a" /* BookingService */],
                __WEBPACK_IMPORTED_MODULE_19__pages_contracts_contract_service__["a" /* ContractService */],
                __WEBPACK_IMPORTED_MODULE_22__auth_keycloak_http__["a" /* KEYCLOAK_HTTP_PROVIDER */],
                __WEBPACK_IMPORTED_MODULE_23__auth_keycloak_service__["a" /* KeycloakService */],
                __WEBPACK_IMPORTED_MODULE_24__services_LogoService__["a" /* LogoService */],
                __WEBPACK_IMPORTED_MODULE_28__services_PaymentService__["a" /* PaymentService */],
                __WEBPACK_IMPORTED_MODULE_5__ionic_native_splash_screen__["a" /* SplashScreen */],
                __WEBPACK_IMPORTED_MODULE_6__ionic_native_status_bar__["a" /* StatusBar */],
                { provide: __WEBPACK_IMPORTED_MODULE_2__angular_core__["u" /* ErrorHandler */], useClass: __WEBPACK_IMPORTED_MODULE_4_ionic_angular__["c" /* IonicErrorHandler */] },
            ]
        })
    ], AppModule);
    return AppModule;
}());

//# sourceMappingURL=app.module.js.map

/***/ }),

/***/ 62:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return BankAccountService; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__app_app_config__ = __webpack_require__(23);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__angular_http__ = __webpack_require__(33);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4_rxjs__ = __webpack_require__(172);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4_rxjs___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_4_rxjs__);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};





var BankAccountService = /** @class */ (function () {
    function BankAccountService(http) {
        this.http = http;
        this.bookingsChangedObservable = new __WEBPACK_IMPORTED_MODULE_4_rxjs__["Subject"]();
    }
    BankAccountService.prototype.getBankAccounts = function (accessId) {
        return this.http.get(__WEBPACK_IMPORTED_MODULE_1__app_app_config__["a" /* AppConfig */].api_url + "/bankaccesses/" + accessId + "/accounts")
            .map(function (res) { return res.json()._embedded.bankAccountEntityList; })
            .catch(this.handleError);
    };
    BankAccountService.prototype.syncBookings = function (accessId, accountId, pin) {
        var _this = this;
        return this.http.put(__WEBPACK_IMPORTED_MODULE_1__app_app_config__["a" /* AppConfig */].api_url + "/bankaccesses/" + accessId + "/accounts/" + accountId + "/sync", pin)
            .map(function (res) {
            _this.bookingsChangedObservable.next(true);
            return res.json()._embedded != null ? res.json()._embedded.bookingEntityList : [];
        })
            .catch(this.handleError);
    };
    BankAccountService.prototype.handleError = function (error) {
        console.error(error);
        var errorJson = error.json();
        if (errorJson) {
            return __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__["Observable"].throw(errorJson || 'Server error');
        }
        else {
            return __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__["Observable"].throw(error || 'Server error');
        }
    };
    BankAccountService = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["A" /* Injectable */])(),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_2__angular_http__["c" /* Http */]])
    ], BankAccountService);
    return BankAccountService;
}());

//# sourceMappingURL=bankAccountService.js.map

/***/ }),

/***/ 693:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return CyclePipe; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};

var CyclePipe = /** @class */ (function () {
    function CyclePipe() {
    }
    CyclePipe.prototype.transform = function (value) {
        switch (value) {
            case 'WEEKLY':
                return "weekly";
            case 'MONTHLY':
                return 'monthly';
            case 'TWO_MONTHLY':
                return 'bimonthly';
            case 'QUARTERLY':
                return 'quarterly';
            case 'HALFYEARLY':
                return 'half-yearly';
            case 'YEARLY':
                return 'yearly';
            default:
                return value;
        }
    };
    CyclePipe = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["S" /* Pipe */])({
            name: 'cycle'
        })
    ], CyclePipe);
    return CyclePipe;
}());

//# sourceMappingURL=cycle.pipe.js.map

/***/ }),

/***/ 694:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* unused harmony export KeycloakHttp */
/* unused harmony export keycloakHttpFactory */
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return KEYCLOAK_HTTP_PROVIDER; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__angular_http__ = __webpack_require__(33);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__keycloak_service__ = __webpack_require__(153);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__);
var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};




/**
 * This provides a wrapper over the ng2 Http class that insures tokens are refreshed on each request.
 */
var KeycloakHttp = /** @class */ (function (_super) {
    __extends(KeycloakHttp, _super);
    function KeycloakHttp(_backend, _defaultOptions, _keycloakService) {
        var _this = _super.call(this, _backend, _defaultOptions) || this;
        _this._keycloakService = _keycloakService;
        return _this;
    }
    KeycloakHttp.prototype.request = function (url, options) {
        var _this = this;
        var tokenPromise = this._keycloakService.getToken();
        var tokenObservable = __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__["Observable"].fromPromise(tokenPromise);
        if (typeof url === 'string') {
            return tokenObservable.map(function (token) {
                var authOptions = new __WEBPACK_IMPORTED_MODULE_1__angular_http__["f" /* RequestOptions */]({ headers: new __WEBPACK_IMPORTED_MODULE_1__angular_http__["b" /* Headers */]({ 'Authorization': 'Bearer ' + token }) });
                return new __WEBPACK_IMPORTED_MODULE_1__angular_http__["f" /* RequestOptions */]().merge(options).merge(authOptions);
            }).concatMap(function (opts) { return _super.prototype.request.call(_this, url, opts); });
        }
        else if (url instanceof __WEBPACK_IMPORTED_MODULE_1__angular_http__["e" /* Request */]) {
            return tokenObservable.map(function (token) {
                url.headers.set('Authorization', 'Bearer ' + token);
                return url;
            }).concatMap(function (request) { return _super.prototype.request.call(_this, request); });
        }
    };
    KeycloakHttp = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["A" /* Injectable */])(),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_1__angular_http__["a" /* ConnectionBackend */], __WEBPACK_IMPORTED_MODULE_1__angular_http__["f" /* RequestOptions */], __WEBPACK_IMPORTED_MODULE_2__keycloak_service__["a" /* KeycloakService */]])
    ], KeycloakHttp);
    return KeycloakHttp;
}(__WEBPACK_IMPORTED_MODULE_1__angular_http__["c" /* Http */]));

function keycloakHttpFactory(backend, defaultOptions, keycloakService) {
    return new KeycloakHttp(backend, defaultOptions, keycloakService);
}
var KEYCLOAK_HTTP_PROVIDER = {
    provide: __WEBPACK_IMPORTED_MODULE_1__angular_http__["c" /* Http */],
    useFactory: keycloakHttpFactory,
    deps: [__WEBPACK_IMPORTED_MODULE_1__angular_http__["g" /* XHRBackend */], __WEBPACK_IMPORTED_MODULE_1__angular_http__["f" /* RequestOptions */], __WEBPACK_IMPORTED_MODULE_2__keycloak_service__["a" /* KeycloakService */]]
};
//# sourceMappingURL=keycloak.http.js.map

/***/ }),

/***/ 695:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return MyApp; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_ionic_angular__ = __webpack_require__(20);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__ionic_native_status_bar__ = __webpack_require__(348);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__ionic_native_splash_screen__ = __webpack_require__(345);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__pages_bankaccess_bankAccessList__ = __webpack_require__(353);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};





var MyApp = /** @class */ (function () {
    function MyApp(platform, statusBar, splashScreen) {
        this.rootPage = __WEBPACK_IMPORTED_MODULE_4__pages_bankaccess_bankAccessList__["a" /* BankAccessListPage */];
        platform.ready().then(function () {
            // Okay, so the platform is ready and our plugins are available.
            // Here you can do any higher level native things you might need.
            statusBar.styleDefault();
            splashScreen.hide();
        });
    }
    MyApp = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["m" /* Component */])({template:/*ion-inline-start:"/Users/alexg/git/multibanking/multibanking-app/src/app/app.html"*/'<ion-nav [root]="rootPage"></ion-nav>\n'/*ion-inline-end:"/Users/alexg/git/multibanking/multibanking-app/src/app/app.html"*/
        }),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_1_ionic_angular__["i" /* Platform */], __WEBPACK_IMPORTED_MODULE_2__ionic_native_status_bar__["a" /* StatusBar */], __WEBPACK_IMPORTED_MODULE_3__ionic_native_splash_screen__["a" /* SplashScreen */]])
    ], MyApp);
    return MyApp;
}());

//# sourceMappingURL=app.component.js.map

/***/ }),

/***/ 86:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return BankAccessService; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(1);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__app_app_config__ = __webpack_require__(23);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__angular_http__ = __webpack_require__(33);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};




var BankAccessService = /** @class */ (function () {
    function BankAccessService(http) {
        this.http = http;
    }
    BankAccessService.prototype.getBankAccesses = function () {
        return this.http.get(__WEBPACK_IMPORTED_MODULE_1__app_app_config__["a" /* AppConfig */].api_url + "/bankaccesses")
            .map(function (res) {
            return res.json()._embedded != null ? res.json()._embedded.bankAccessEntityList : [];
        })
            .catch(this.handleError);
    };
    BankAccessService.prototype.createBankAcccess = function (bankaccess) {
        return this.http.post(__WEBPACK_IMPORTED_MODULE_1__app_app_config__["a" /* AppConfig */].api_url + "/bankaccesses", bankaccess)
            .catch(this.handleError);
    };
    BankAccessService.prototype.updateBankAcccess = function (bankaccess) {
        return this.http.put(__WEBPACK_IMPORTED_MODULE_1__app_app_config__["a" /* AppConfig */].api_url + "/bankaccesses/" + bankaccess.id, bankaccess)
            .catch(this.handleError);
    };
    BankAccessService.prototype.deleteBankAccess = function (accessId) {
        return this.http.delete(__WEBPACK_IMPORTED_MODULE_1__app_app_config__["a" /* AppConfig */].api_url + "/bankaccesses/" + accessId)
            .catch(this.handleError);
    };
    BankAccessService.prototype.handleError = function (error) {
        console.error(error);
        var errorJson = error.json();
        if (errorJson) {
            return __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__["Observable"].throw(errorJson || 'Server error');
        }
        else {
            return __WEBPACK_IMPORTED_MODULE_3_rxjs_Observable__["Observable"].throw(error || 'Server error');
        }
    };
    BankAccessService = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["A" /* Injectable */])(),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_2__angular_http__["c" /* Http */]])
    ], BankAccessService);
    return BankAccessService;
}());

//# sourceMappingURL=bankAccessService.js.map

/***/ })

},[365]);
//# sourceMappingURL=main.js.map