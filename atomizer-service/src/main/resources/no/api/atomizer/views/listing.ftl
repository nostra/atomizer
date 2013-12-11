<#-- @ftlvariable name="" type="no.api.atomizer.views.IndexView" -->
<#if feed.entries??>
    <table class="table">
        <thead>
        <tr>
            <th>Path</th>
            <th>Update</th>
            <th>Age</th>
        </tr>
        </thead>
        <tbody>
        <#list feed.entries as entry>
            <tr>
                <td><a href="event/${entry.id}">
                    <#list entry.links as link>${link!}</#list>
                </td>
                <td>${entry.updated?datetime}</td>
                <td>${entry.age}</td>
            </tr>
         </#list>
        </tbody>
    </table>
</#if>