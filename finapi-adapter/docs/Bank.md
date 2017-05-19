
# Bank

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **Long** | Bank identifier | 
**name** | **String** | Name of bank | 
**loginHint** | **String** | Login hint. Contains a German message for the user that explains what kind of credentials are expected.&lt;br/&gt;&lt;br/&gt;Please note that it is strongly recommended to always show the login hint to the user if there is one, as the credentials that finAPI requires for the bank might be different to the credentials that the user knows from the bank&#39;s website.&lt;br/&gt;&lt;br/&gt;Also note that the contents of this field should always be interpreted as HTML, as the text might contain HTML tags for highlighted words, paragraphs, etc. |  [optional]
**bic** | **String** | BIC of bank |  [optional]
**blz** | **String** | BLZ of bank | 
**blzs** | **List&lt;String&gt;** | List of BLZs that belong to this bank. NOTE: This field is deprecated and will be removed at some point. Please refer to field &#39;blz&#39; instead. | 
**loginFieldUserId** | **String** | Label for the user ID login field, as it is called on the bank&#39;s website (e.g. \&quot;Nutzerkennung\&quot;). If this field is set (i.e. not null) then you should prompt your users to enter the required data in a text field which you can label with this field&#39;s value. |  [optional]
**loginFieldCustomerId** | **String** | Label for the customer ID login field, as it is called on the bank&#39;s website (e.g. \&quot;Kundennummer\&quot;). If this field is set (i.e. not null) then you should prompt your users to enter the required data in a text field which you can label with this field&#39;s value. |  [optional]
**loginFieldPin** | **String** | Label for the PIN field, as it is called on the bank&#39;s website (mostly \&quot;PIN\&quot;). If this field is set (i.e. not null) then you should prompt your users to enter the required data in a text field which you can label with this field&#39;s value. |  [optional]
**isSupported** | **Boolean** | Whether this bank is supported by finAPI, i.e. whether you can import/update a bank connection of this bank. | 
**supportedDataSources** | [**List&lt;SupportedDataSourcesEnum&gt;**](#List&lt;SupportedDataSourcesEnum&gt;) | List of the data sources that finAPI will use for data download for this bank. Possible values:&lt;/br&gt;&lt;/br&gt; - FINTS_SERVER - means that finAPI will download data via the bank&#39;s FinTS interface.&lt;/br&gt; - WEB_SCRAPER - means that finAPI will parse data from the bank&#39;s online banking website.&lt;/br&gt;&lt;/br&gt;Note that this list will be empty for non-supported banks. Note also that web scraping might be disabled for your client (see GET /clientConfiguration). When this is the case, then finAPI will not use the web scraper for data download, and if the web scraper is the only supported data source of this bank, then finAPI will not allow to download any data for this bank at all (for details, see POST /bankConnections/import and POST /bankConnections/update). | 


<a name="List<SupportedDataSourcesEnum>"></a>
## Enum: List&lt;SupportedDataSourcesEnum&gt;
Name | Value
---- | -----
WEB_SCRAPER | &quot;WEB_SCRAPER&quot;
FINTS_SERVER | &quot;FINTS_SERVER&quot;



