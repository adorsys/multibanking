# swagger-java-client

## Requirements

Building the API client library requires [Maven](https://maven.apache.org/) to be installed.

## Installation

To install the API client library to your local Maven repository, simply execute:

```shell
mvn install
```

To deploy it to a remote Maven repository instead, configure the settings of the repository and execute:

```shell
mvn deploy
```

Refer to the [official documentation](https://maven.apache.org/plugins/maven-deploy-plugin/usage.html) for more information.

### Maven users

Add this dependency to your project's POM:

```xml
<dependency>
    <groupId>io.swagger</groupId>
    <artifactId>swagger-java-client</artifactId>
    <version>1.0.0</version>
    <scope>compile</scope>
</dependency>
```

### Gradle users

Add this dependency to your project's build file:

```groovy
compile "io.swagger:swagger-java-client:1.0.0"
```

### Others

At first generate the JAR by executing:

    mvn package

Then manually install the following JARs:

* target/swagger-java-client-1.0.0.jar
* target/lib/*.jar

## Getting Started

Please follow the [installation](#installation) instruction and execute the following Java code:

```java

import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.AccountsApi;

import java.io.File;
import java.util.*;

public class AccountsApiExample {

    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        
        // Configure OAuth2 access token for authorization: finapi_auth
        OAuth finapi_auth = (OAuth) defaultClient.getAuthentication("finapi_auth");
        finapi_auth.setAccessToken("YOUR ACCESS TOKEN");

        AccountsApi apiInstance = new AccountsApi();
        Long id = 789L; // Long | Identifier of the account to delete
        try {
            apiInstance.deleteAccount(id);
        } catch (ApiException e) {
            System.err.println("Exception when calling AccountsApi#deleteAccount");
            e.printStackTrace();
        }
    }
}

```

## Documentation for API Endpoints

All URIs are relative to *https://localhost/*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*AccountsApi* | [**deleteAccount**](docs/AccountsApi.md#deleteAccount) | **DELETE** /api/v1/accounts/{id} | Delete an account
*AccountsApi* | [**deleteAllAccounts**](docs/AccountsApi.md#deleteAllAccounts) | **DELETE** /api/v1/accounts | Delete all accounts
*AccountsApi* | [**editAccount**](docs/AccountsApi.md#editAccount) | **PATCH** /api/v1/accounts/{id} | Edit an account
*AccountsApi* | [**executeSepaMoneyTransfer**](docs/AccountsApi.md#executeSepaMoneyTransfer) | **POST** /api/v1/accounts/executeSepaMoneyTransfer | Execute SEPA Money Transfer
*AccountsApi* | [**getAccount**](docs/AccountsApi.md#getAccount) | **GET** /api/v1/accounts/{id} | Get an account
*AccountsApi* | [**getAndSearchAllAccounts**](docs/AccountsApi.md#getAndSearchAllAccounts) | **GET** /api/v1/accounts | Get and search all accounts
*AccountsApi* | [**getDailyBalances**](docs/AccountsApi.md#getDailyBalances) | **GET** /api/v1/accounts/dailyBalances | Get daily balances
*AccountsApi* | [**getMultipleAccounts**](docs/AccountsApi.md#getMultipleAccounts) | **GET** /api/v1/accounts/{ids} | Get multiple accounts
*AccountsApi* | [**requestSepaMoneyTransfer**](docs/AccountsApi.md#requestSepaMoneyTransfer) | **POST** /api/v1/accounts/requestSepaMoneyTransfer | Request SEPA Money Transfer
*AuthorizationApi* | [**getToken**](docs/AuthorizationApi.md#getToken) | **POST** /oauth/token | Get tokens
*AuthorizationApi* | [**revokeToken**](docs/AuthorizationApi.md#revokeToken) | **POST** /oauth/revoke | Revoke a token
*BankConnectionsApi* | [**deleteAllBankConnections**](docs/BankConnectionsApi.md#deleteAllBankConnections) | **DELETE** /api/v1/bankConnections | Delete all bank connections
*BankConnectionsApi* | [**deleteBankConnection**](docs/BankConnectionsApi.md#deleteBankConnection) | **DELETE** /api/v1/bankConnections/{id} | Delete a bank connection
*BankConnectionsApi* | [**editBankConnection**](docs/BankConnectionsApi.md#editBankConnection) | **PATCH** /api/v1/bankConnections/{id} | Edit a bank connection
*BankConnectionsApi* | [**getAllBankConnections**](docs/BankConnectionsApi.md#getAllBankConnections) | **GET** /api/v1/bankConnections | Get all bank connections
*BankConnectionsApi* | [**getBankConnection**](docs/BankConnectionsApi.md#getBankConnection) | **GET** /api/v1/bankConnections/{id} | Get a bank connection
*BankConnectionsApi* | [**getMultipleBankConnections**](docs/BankConnectionsApi.md#getMultipleBankConnections) | **GET** /api/v1/bankConnections/{ids} | Get multiple bank connections
*BankConnectionsApi* | [**importBankConnection**](docs/BankConnectionsApi.md#importBankConnection) | **POST** /api/v1/bankConnections/import | Import a new bank connection
*BankConnectionsApi* | [**updateBankConnection**](docs/BankConnectionsApi.md#updateBankConnection) | **POST** /api/v1/bankConnections/update | Update a bank connection
*BanksApi* | [**getAndSearchAllBanks**](docs/BanksApi.md#getAndSearchAllBanks) | **GET** /api/v1/banks | Get and search all banks
*BanksApi* | [**getBank**](docs/BanksApi.md#getBank) | **GET** /api/v1/banks/{id} | Get a bank
*BanksApi* | [**getMultipleBanks**](docs/BanksApi.md#getMultipleBanks) | **GET** /api/v1/banks/{ids} | Get multiple banks
*CategoriesApi* | [**createCategory**](docs/CategoriesApi.md#createCategory) | **POST** /api/v1/categories | Create a new category
*CategoriesApi* | [**deleteAllCategories**](docs/CategoriesApi.md#deleteAllCategories) | **DELETE** /api/v1/categories | Delete all categories
*CategoriesApi* | [**deleteCategory**](docs/CategoriesApi.md#deleteCategory) | **DELETE** /api/v1/categories/{id} | Delete a category
*CategoriesApi* | [**getAndSearchAllCategories**](docs/CategoriesApi.md#getAndSearchAllCategories) | **GET** /api/v1/categories | Get and search all categories
*CategoriesApi* | [**getCashFlows**](docs/CategoriesApi.md#getCashFlows) | **GET** /api/v1/categories/cashFlows | Get cash flows
*CategoriesApi* | [**getCategory**](docs/CategoriesApi.md#getCategory) | **GET** /api/v1/categories/{id} | Get a category
*CategoriesApi* | [**getMultipleCategories**](docs/CategoriesApi.md#getMultipleCategories) | **GET** /api/v1/categories/{ids} | Get multiple categories
*ClientConfigurationApi* | [**editClientConfiguration**](docs/ClientConfigurationApi.md#editClientConfiguration) | **PATCH** /api/v1/clientConfiguration | Edit client configuration
*ClientConfigurationApi* | [**getClientConfiguration**](docs/ClientConfigurationApi.md#getClientConfiguration) | **GET** /api/v1/clientConfiguration | Get client configuration
*LabelsApi* | [**createLabel**](docs/LabelsApi.md#createLabel) | **POST** /api/v1/labels | Create a new label
*LabelsApi* | [**deleteAllLabels**](docs/LabelsApi.md#deleteAllLabels) | **DELETE** /api/v1/labels | Delete all labels
*LabelsApi* | [**deleteLabel**](docs/LabelsApi.md#deleteLabel) | **DELETE** /api/v1/labels/{id} | Delete a label
*LabelsApi* | [**editLabel**](docs/LabelsApi.md#editLabel) | **PATCH** /api/v1/labels/{id} | Edit a label
*LabelsApi* | [**getAndSearchAllLabels**](docs/LabelsApi.md#getAndSearchAllLabels) | **GET** /api/v1/labels | Get and search all labels
*LabelsApi* | [**getLabel**](docs/LabelsApi.md#getLabel) | **GET** /api/v1/labels/{id} | Get a label
*LabelsApi* | [**getMultipleLabels**](docs/LabelsApi.md#getMultipleLabels) | **GET** /api/v1/labels/{ids} | Get multiple labels
*MandatorAdministrationApi* | [**deleteUsers**](docs/MandatorAdministrationApi.md#deleteUsers) | **POST** /api/v1/mandatorAdmin/deleteUsers | Delete users
*MandatorAdministrationApi* | [**getUserList**](docs/MandatorAdministrationApi.md#getUserList) | **GET** /api/v1/mandatorAdmin/getUserList | Get user list
*MocksAndTestsApi* | [**mockBatchUpdate**](docs/MocksAndTestsApi.md#mockBatchUpdate) | **POST** /api/v1/tests/mockBatchUpdate | Mock batch update
*NotificationRulesApi* | [**createNotificationRule**](docs/NotificationRulesApi.md#createNotificationRule) | **POST** /api/v1/notificationRules | Create a new notification rule
*NotificationRulesApi* | [**deleteAllNotificationRules**](docs/NotificationRulesApi.md#deleteAllNotificationRules) | **DELETE** /api/v1/notificationRules | Delete all notification rules
*NotificationRulesApi* | [**deleteNotificationRule**](docs/NotificationRulesApi.md#deleteNotificationRule) | **DELETE** /api/v1/notificationRules/{id} | Delete a notification rule
*NotificationRulesApi* | [**getAndSearchAllNotificationRules**](docs/NotificationRulesApi.md#getAndSearchAllNotificationRules) | **GET** /api/v1/notificationRules | Get and search all notification rules
*NotificationRulesApi* | [**getNotificationRule**](docs/NotificationRulesApi.md#getNotificationRule) | **GET** /api/v1/notificationRules/{id} | Get a notification rule
*SecuritiesApi* | [**getAndSearchAllSecurities**](docs/SecuritiesApi.md#getAndSearchAllSecurities) | **GET** /api/v1/securities | Get and search all securities
*SecuritiesApi* | [**getMultipleSecurities**](docs/SecuritiesApi.md#getMultipleSecurities) | **GET** /api/v1/securities/{ids} | Get multiple securities
*SecuritiesApi* | [**getSecurity**](docs/SecuritiesApi.md#getSecurity) | **GET** /api/v1/securities/{id} | Get a security
*TransactionsApi* | [**deleteAllTransactions**](docs/TransactionsApi.md#deleteAllTransactions) | **DELETE** /api/v1/transactions | Delete all transactions
*TransactionsApi* | [**deleteTransaction**](docs/TransactionsApi.md#deleteTransaction) | **DELETE** /api/v1/transactions/{id} | Delete a transaction
*TransactionsApi* | [**editMultipleTransactions**](docs/TransactionsApi.md#editMultipleTransactions) | **PATCH** /api/v1/transactions | Edit multiple transactions
*TransactionsApi* | [**editMultipleTransactionsDeprecated**](docs/TransactionsApi.md#editMultipleTransactionsDeprecated) | **PATCH** /api/v1/transactions/{ids} | Edit multiple transactions (DEPRECATED)
*TransactionsApi* | [**editTransaction**](docs/TransactionsApi.md#editTransaction) | **PATCH** /api/v1/transactions/{id} | Edit a transaction
*TransactionsApi* | [**getAndSearchAllTransactions**](docs/TransactionsApi.md#getAndSearchAllTransactions) | **GET** /api/v1/transactions | Get and search all transactions
*TransactionsApi* | [**getMultipleTransactions**](docs/TransactionsApi.md#getMultipleTransactions) | **GET** /api/v1/transactions/{ids} | Get multiple transactions
*TransactionsApi* | [**getTransaction**](docs/TransactionsApi.md#getTransaction) | **GET** /api/v1/transactions/{id} | Get a transaction
*TransactionsApi* | [**restoreTransaction**](docs/TransactionsApi.md#restoreTransaction) | **POST** /api/v1/transactions/{id}/restore | Restore a transaction
*TransactionsApi* | [**splitTransaction**](docs/TransactionsApi.md#splitTransaction) | **POST** /api/v1/transactions/{id}/split | Split a transaction
*TransactionsApi* | [**triggerCategorization**](docs/TransactionsApi.md#triggerCategorization) | **POST** /api/v1/transactions/triggerCategorization | Trigger categorization
*UsersApi* | [**createUser**](docs/UsersApi.md#createUser) | **POST** /api/v1/users | Create a new user
*UsersApi* | [**deleteAuthorizedUser**](docs/UsersApi.md#deleteAuthorizedUser) | **DELETE** /api/v1/users | Delete the authorized user
*UsersApi* | [**deleteUnverifiedUser**](docs/UsersApi.md#deleteUnverifiedUser) | **DELETE** /api/v1/users/{userId} | Delete an unverified user
*UsersApi* | [**editAuthorizedUser**](docs/UsersApi.md#editAuthorizedUser) | **PATCH** /api/v1/users | Edit the authorized user
*UsersApi* | [**executePasswordChange**](docs/UsersApi.md#executePasswordChange) | **POST** /api/v1/users/executePasswordChange | Execute password change
*UsersApi* | [**getAuthorizedUser**](docs/UsersApi.md#getAuthorizedUser) | **GET** /api/v1/users | Get the authorized user
*UsersApi* | [**getVerificationStatus**](docs/UsersApi.md#getVerificationStatus) | **GET** /api/v1/users/verificationStatus | Get a user&#39;s verification status
*UsersApi* | [**requestPasswordChange**](docs/UsersApi.md#requestPasswordChange) | **POST** /api/v1/users/requestPasswordChange | Request password change
*UsersApi* | [**verifyUser**](docs/UsersApi.md#verifyUser) | **POST** /api/v1/users/verify/{userId} | Verify a user


## Documentation for Models

 - [AccessToken](docs/AccessToken.md)
 - [Account](docs/Account.md)
 - [AccountList](docs/AccountList.md)
 - [AccountParams](docs/AccountParams.md)
 - [Apiv1accountsrequestSepaMoneyTransferAdditionalMoneyTransfers](docs/Apiv1accountsrequestSepaMoneyTransferAdditionalMoneyTransfers.md)
 - [Apiv1testsmockBatchUpdateMockAccountsData](docs/Apiv1testsmockBatchUpdateMockAccountsData.md)
 - [Apiv1testsmockBatchUpdateMockBankConnectionUpdates](docs/Apiv1testsmockBatchUpdateMockBankConnectionUpdates.md)
 - [Apiv1testsmockBatchUpdateNewTransactions](docs/Apiv1testsmockBatchUpdateNewTransactions.md)
 - [Apiv1transactionsidsplitSubTransactions](docs/Apiv1transactionsidsplitSubTransactions.md)
 - [BadCredentialsError](docs/BadCredentialsError.md)
 - [Bank](docs/Bank.md)
 - [BankConnection](docs/BankConnection.md)
 - [BankConnectionList](docs/BankConnectionList.md)
 - [BankList](docs/BankList.md)
 - [BankResponse](docs/BankResponse.md)
 - [Body](docs/Body.md)
 - [Body1](docs/Body1.md)
 - [Body10](docs/Body10.md)
 - [Body11](docs/Body11.md)
 - [Body12](docs/Body12.md)
 - [Body13](docs/Body13.md)
 - [Body14](docs/Body14.md)
 - [Body15](docs/Body15.md)
 - [Body16](docs/Body16.md)
 - [Body17](docs/Body17.md)
 - [Body18](docs/Body18.md)
 - [Body19](docs/Body19.md)
 - [Body2](docs/Body2.md)
 - [Body20](docs/Body20.md)
 - [Body21](docs/Body21.md)
 - [Body3](docs/Body3.md)
 - [Body4](docs/Body4.md)
 - [Body5](docs/Body5.md)
 - [Body6](docs/Body6.md)
 - [Body7](docs/Body7.md)
 - [Body8](docs/Body8.md)
 - [Body9](docs/Body9.md)
 - [CashFlow](docs/CashFlow.md)
 - [CashFlowList](docs/CashFlowList.md)
 - [Category](docs/Category.md)
 - [CategoryList](docs/CategoryList.md)
 - [CategoryParams](docs/CategoryParams.md)
 - [ClientConfiguration](docs/ClientConfiguration.md)
 - [ClientConfigurationParams](docs/ClientConfigurationParams.md)
 - [DailyBalance](docs/DailyBalance.md)
 - [DailyBalanceList](docs/DailyBalanceList.md)
 - [EditBankConnectionParams](docs/EditBankConnectionParams.md)
 - [ErrorDetails](docs/ErrorDetails.md)
 - [ErrorMessage](docs/ErrorMessage.md)
 - [ExecutePasswordChangeParams](docs/ExecutePasswordChangeParams.md)
 - [ExecuteSepaMoneyTransferParams](docs/ExecuteSepaMoneyTransferParams.md)
 - [IdentifierList](docs/IdentifierList.md)
 - [ImportBankConnectionParams](docs/ImportBankConnectionParams.md)
 - [InlineResponse200](docs/InlineResponse200.md)
 - [InlineResponse2001](docs/InlineResponse2001.md)
 - [InlineResponse20010](docs/InlineResponse20010.md)
 - [InlineResponse20011](docs/InlineResponse20011.md)
 - [InlineResponse20012](docs/InlineResponse20012.md)
 - [InlineResponse20012Labels](docs/InlineResponse20012Labels.md)
 - [InlineResponse20013](docs/InlineResponse20013.md)
 - [InlineResponse20014](docs/InlineResponse20014.md)
 - [InlineResponse20015](docs/InlineResponse20015.md)
 - [InlineResponse20015Users](docs/InlineResponse20015Users.md)
 - [InlineResponse20016](docs/InlineResponse20016.md)
 - [InlineResponse20016NotificationRules](docs/InlineResponse20016NotificationRules.md)
 - [InlineResponse20017](docs/InlineResponse20017.md)
 - [InlineResponse20017Securities](docs/InlineResponse20017Securities.md)
 - [InlineResponse20018](docs/InlineResponse20018.md)
 - [InlineResponse20019](docs/InlineResponse20019.md)
 - [InlineResponse20019Transactions](docs/InlineResponse20019Transactions.md)
 - [InlineResponse2002](docs/InlineResponse2002.md)
 - [InlineResponse20020](docs/InlineResponse20020.md)
 - [InlineResponse20021](docs/InlineResponse20021.md)
 - [InlineResponse20022](docs/InlineResponse20022.md)
 - [InlineResponse20023](docs/InlineResponse20023.md)
 - [InlineResponse20024](docs/InlineResponse20024.md)
 - [InlineResponse2002DailyBalances](docs/InlineResponse2002DailyBalances.md)
 - [InlineResponse2002Paging](docs/InlineResponse2002Paging.md)
 - [InlineResponse2003](docs/InlineResponse2003.md)
 - [InlineResponse2004](docs/InlineResponse2004.md)
 - [InlineResponse2005](docs/InlineResponse2005.md)
 - [InlineResponse2005Bank](docs/InlineResponse2005Bank.md)
 - [InlineResponse2005Connections](docs/InlineResponse2005Connections.md)
 - [InlineResponse2005LastManualUpdate](docs/InlineResponse2005LastManualUpdate.md)
 - [InlineResponse2005TwoStepProcedures](docs/InlineResponse2005TwoStepProcedures.md)
 - [InlineResponse2006](docs/InlineResponse2006.md)
 - [InlineResponse2007](docs/InlineResponse2007.md)
 - [InlineResponse2008](docs/InlineResponse2008.md)
 - [InlineResponse2008Categories](docs/InlineResponse2008Categories.md)
 - [InlineResponse2009](docs/InlineResponse2009.md)
 - [InlineResponse2009CashFlows](docs/InlineResponse2009CashFlows.md)
 - [InlineResponse200Accounts](docs/InlineResponse200Accounts.md)
 - [InlineResponse401](docs/InlineResponse401.md)
 - [InlineResponse403](docs/InlineResponse403.md)
 - [InlineResponse403Errors](docs/InlineResponse403Errors.md)
 - [Label](docs/Label.md)
 - [LabelList](docs/LabelList.md)
 - [LabelParams](docs/LabelParams.md)
 - [MockAccountData](docs/MockAccountData.md)
 - [MockBankConnectionUpdate](docs/MockBankConnectionUpdate.md)
 - [MockBatchUpdateParams](docs/MockBatchUpdateParams.md)
 - [MoneyTransferOrderingResponse](docs/MoneyTransferOrderingResponse.md)
 - [NewTransaction](docs/NewTransaction.md)
 - [NotificationRule](docs/NotificationRule.md)
 - [NotificationRuleList](docs/NotificationRuleList.md)
 - [NotificationRuleParams](docs/NotificationRuleParams.md)
 - [PageableBankList](docs/PageableBankList.md)
 - [PageableCategoryList](docs/PageableCategoryList.md)
 - [PageableLabelList](docs/PageableLabelList.md)
 - [PageableSecurityList](docs/PageableSecurityList.md)
 - [PageableTransactionList](docs/PageableTransactionList.md)
 - [PageableUserInfoList](docs/PageableUserInfoList.md)
 - [Paging](docs/Paging.md)
 - [PasswordChangingResource](docs/PasswordChangingResource.md)
 - [RequestPasswordChangeParameters](docs/RequestPasswordChangeParameters.md)
 - [RequestSepaMoneyTransferParams](docs/RequestSepaMoneyTransferParams.md)
 - [Security](docs/Security.md)
 - [SecurityList](docs/SecurityList.md)
 - [SingleMoneyTransferRecipientData](docs/SingleMoneyTransferRecipientData.md)
 - [SplitTransactionsParams](docs/SplitTransactionsParams.md)
 - [SubTransactionParams](docs/SubTransactionParams.md)
 - [Transaction](docs/Transaction.md)
 - [TransactionList](docs/TransactionList.md)
 - [TriggerCategorizationParams](docs/TriggerCategorizationParams.md)
 - [TwoStepProcedure](docs/TwoStepProcedure.md)
 - [UpdateBankConnectionParams](docs/UpdateBankConnectionParams.md)
 - [UpdateMultipleTransactionsParams](docs/UpdateMultipleTransactionsParams.md)
 - [UpdateResult](docs/UpdateResult.md)
 - [UpdateTransactionsParams](docs/UpdateTransactionsParams.md)
 - [User](docs/User.md)
 - [UserCreateParamsImpl](docs/UserCreateParamsImpl.md)
 - [UserIdentifiersList](docs/UserIdentifiersList.md)
 - [UserIdentifiersParams](docs/UserIdentifiersParams.md)
 - [UserInfo](docs/UserInfo.md)
 - [UserUpdateParamsImpl](docs/UserUpdateParamsImpl.md)
 - [VerificationStatusResource](docs/VerificationStatusResource.md)


## Documentation for Authorization

Authentication schemes defined for the API:
### finapi_auth

- **Type**: OAuth
- **Flow**: accessCode
- **Authorizatoin URL**: /oauth/authorize
- **Scopes**: 
  - all: modify any sources


## Recommendation

It's recommended to create an instance of `ApiClient` per thread in a multithreaded environment to avoid any potential issues.

## Author



