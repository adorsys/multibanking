
# WebForm

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **Long** | Web form identifier, as returned in the 451 response of the REST service call that initiated the web form flow. | 
**token** | **String** | Token for the finAPI web form page, as contained in the 451 response of the REST service call that initiated the web form flow (in the &#39;Location&#39; header). | 
**status** | [**StatusEnum**](#StatusEnum) | Status of a web form. Possible values are:&lt;br/&gt;&amp;bull; NOT_YET_OPENED - the web form URL was not yet called;&lt;br/&gt;&amp;bull; IN_PROGRESS - the web form has been opened but not yet submitted by the user;&lt;br/&gt;&amp;bull; COMPLETED - the user has opened and submitted the web form;&lt;br/&gt;&amp;bull; ABORTED - the user has opened but then aborted the web form, or the web form was aborted by the finAPI system because it has expired (this is the case when a web form is opened and then not submitted within 20 minutes) | 
**serviceResponseCode** | **Integer** | HTTP response code of the REST service call that initiated the web form flow. This field can be queried as soon as the status becomes COMPLETED or ABORTED. Note that it is still not guaranteed in this case that the field has a value, i.e. it might be null. |  [optional]
**serviceResponseBody** | **String** | HTTP response body of the REST service call that initiated the web form flow. This field can be queried as soon as the status becomes COMPLETED or ABORTED. Note that it is still not guaranteed in this case that the field has a value, i.e. it might be null. |  [optional]


<a name="StatusEnum"></a>
## Enum: StatusEnum
Name | Value
---- | -----
NOT_YET_OPENED | &quot;NOT_YET_OPENED&quot;
IN_PROGRESS | &quot;IN_PROGRESS&quot;
COMPLETED | &quot;COMPLETED&quot;
ABORTED | &quot;ABORTED&quot;



