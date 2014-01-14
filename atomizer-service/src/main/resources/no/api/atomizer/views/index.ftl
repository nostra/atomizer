<#-- @ftlvariable name="" type="no.api.atomizer.views.IndexView" -->
<#include "top.ftl" />

<!-- content -->
<div class="container">
    <div class="row-fluid">
        <div class="span4">
            <div class="head-box"><h2>Mark Path Stale</h2></div>
            <div class="content-box">
                <form class="set-margin2" action="submit.html" method="post" commandName="sgsubmit">
                    <fieldset>
                        <label>Path :</label>
                        <input id="spath1" size="16" name="path" path="path" cssClass="span12" cssErrorClass="error" placeholder="Path"/>
                        <input class="btn set-margin3" type="submit" value="Mark"/>
                    </fieldset>
                </form>
            </div>
        </div>
    </div>

    <div class="row-fluid"><!-- table show path -->
        <div class="span12">
            <div class="head-box space-height">
                <h2>Path List</h2>
                <div class="pull-right">
                    <form class="form-horizontal set-margin2" name="path" action="search.html" method="post" commandName="sgsearch">
                        <label class="control-label">Search for path :&nbsp;</label>
                        <div class="input-append">
                            <input id="spath2" path="path" name="path" cssErrorClass="error" placeholder="Use .* for wild cards" value="${feed.searchExpression!""}">
                            <button class="btn" type="submit"><i class="icon-search"></i></button>
                        </div>
                    </form>
                </div>
            </div>
            <div class="content-box">
                <#if feed??>
                    <div class="row-fluid set-margin1">
                        <div class="span4">Precision (sec): ${feed.precision}</div>
                        <div class="span4">Lifetime (sec): ${feed.lifetime}</div>
                        <div class="span4">Updated: ${feed.updated?datetime}</div>
                    </div>
                    <#include "listing.ftl" />
                </#if>
            </div>
            <#if metaCounters?? && metaCounters?has_content>
            <div class="content-box">
                <#include "counterlist.ftl" />
            </div>
            </#if>
        </div>
    </div><!-- end table -->
</div><!-- end content -->


<#include "bottom.ftl" />