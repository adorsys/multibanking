
# Bank

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **Long** | Bank identifier.&lt;br/&gt;&lt;br/&gt;NOTE: Do NOT assume that the identifiers of banks are the same across different finAPI environments. In fact, the identifiers may change whenever a new finAPI version is released, even within the same environment. The identifiers are meant to be used for references within the finAPI services only, but not for hard-coding them in your application. If you need to hard-code the usage of a certain bank within your application, please instead refer to the BLZ. | 
**name** | **String** | Name of bank | 
**loginHint** | **String** | Login hint. Contains a German message for the user that explains what kind of credentials are expected.&lt;br/&gt;&lt;br/&gt;Please note that it is strongly recommended to always show the login hint to the user if there is one, as the credentials that finAPI requires for the bank might be different to the credentials that the user knows from the bank&#39;s website.&lt;br/&gt;&lt;br/&gt;Also note that the contents of this field should always be interpreted as HTML, as the text might contain HTML tags for highlighted words, paragraphs, etc. |  [optional]
**bic** | **String** | BIC of bank |  [optional]
**blz** | **String** | BLZ of bank | 
**blzs** | **List&lt;String&gt;** | List of BLZs that belong to this bank. NOTE: This field is deprecated and will be removed at some point. Please refer to field &#39;blz&#39; instead. | 
**loginFieldUserId** | **String** | Label for the user ID login field, as it is called on the bank&#39;s website (e.g. \&quot;Nutzerkennung\&quot;). If this field is set (i.e. not null) then you should prompt your users to enter the required data in a text field which you can label with this field&#39;s value. |  [optional]
**loginFieldCustomerId** | **String** | Label for the customer ID login field, as it is called on the bank&#39;s website (e.g. \&quot;Kundennummer\&quot;). If this field is set (i.e. not null) then you should prompt your users to enter the required data in a text field which you can label with this field&#39;s value. |  [optional]
**loginFieldPin** | **String** | Label for the PIN field, as it is called on the bank&#39;s website (mostly \&quot;PIN\&quot;). If this field is set (i.e. not null) then you should prompt your users to enter the required data in a text field which you can label with this field&#39;s value. |  [optional]
**isCustomerIdPassword** | **Boolean** | Whether the banking customer ID has to be treated like a password. Certain banks require a second password (besides the PIN) for the user to login. In this case your application should use a password input field when prompting users for their credentials. | 
**isSupported** | **Boolean** | Whether this bank is supported by finAPI, i.e. whether you can import/update a bank connection of this bank. | 
**supportedDataSources** | [**List&lt;SupportedDataSourcesEnum&gt;**](#List&lt;SupportedDataSourcesEnum&gt;) | List of the data sources that finAPI will use for data download for this bank. Possible values:&lt;br&gt;&lt;br&gt;&amp;bull; &lt;code&gt;FINTS_SERVER&lt;/code&gt; - means that finAPI will download data via the bank&#39;s FinTS interface.&lt;br&gt;&amp;bull; &lt;code&gt;WEB_SCRAPER&lt;/code&gt; - means that finAPI will parse data from the bank&#39;s online banking website.&lt;br&gt;&lt;br&gt;Note that this list will be empty for non-supported banks. Note also that web scraping might be disabled for your client (see GET /clientConfiguration). When this is the case, then finAPI will not use the web scraper for data download, and if the web scraper is the only supported data source of this bank, then finAPI will not allow to download any data for this bank at all (for details, see POST /bankConnections/import and POST /bankConnections/update). | 
**pinsAreVolatile** | **Boolean** | Whether the PINs that are used for authentication with the bank are volatile. If the PINs are volatile, it means that a PIN is usually valid only for a single authentication, and is then invalidated. If a bank uses volatile PINs, it is strongly inadvisable to store PINs in finAPI, as a stored PIN will not work for future authentications. | 
**location** | **String** | Bank location (two-letter country code; ISO 3166 ALPHA-2). Note that when this field is not set, it means that this bank depicts an international institute which is not bound to any specific country. |  [optional]
**city** | **String** | City that this bank is located in. Note that this field may not be set for some banks. |  [optional]
**isTestBank** | **Boolean** | If true, then this bank does not depict a real bank, but rather a testing endpoint provided by a bank or by finAPI. You probably want to regard these banks only during the development of your application, but not in production. You can filter out these banks in production by making sure that the &#39;isTestBank&#39; parameter is always set to &#39;false&#39; whenever your application is calling the &#39;Get and search all banks&#39; service. | 
**popularity** | **Integer** | Popularity of this bank with your users (mandator-wide, i.e. across all of your clients). The value equals the number of bank connections that are currently imported for this bank across all of your users (which means it is a constantly adjusting value). You can use this field for statistical evaluation, and also for ordering bank search results (see service &#39;Get and search all banks&#39;). | 
**health** | **Integer** | The health status of this bank. This is a value between 0 and 100, depicting the percentage of successful communication attempts with this bank during the latest couple of bank connection imports or updates (across the entire finAPI system). Note that &#39;successful&#39; means that there was no technical error trying to establish a communication with the bank. Non-technical errors (like incorrect credentials) are regarded successful communication attempts. | 
**lastCommunicationAttempt** | **String** | Time of the last communication attempt with this bank during a bank connection import or update (across the entire finAPI system). The value is returned in the format &#39;YYYY-MM-DD HH:MM:SS.SSS&#39; (german time). |  [optional]
**lastSuccessfulCommunication** | **String** | Time of the last successful communication with this bank during a bank connection import or update (across the entire finAPI system). The value is returned in the format &#39;YYYY-MM-DD HH:MM:SS.SSS&#39; (german time). |  [optional]


<a name="List<SupportedDataSourcesEnum>"></a>
## Enum: List&lt;SupportedDataSourcesEnum&gt;
Name | Value
---- | -----
WEB_SCRAPER | &quot;WEB_SCRAPER&quot;
FINTS_SERVER | &quot;FINTS_SERVER&quot;



