<#-- @ftlvariable name="" type="no.api.atomizer.views.IndexView" -->
<script type="text/javascript">
    var xmlhttp;

    function doPut($target){
        xmlhttp=new XMLHttpRequest();
        xmlhttp.open('PUT',$target,true)
        xmlhttp.send(null);
        window.location.reload();
    }

</script>
<table class="table">
    <thead>
    <tr>
        <th>Token</th>
        <th>Count</th>
        <th>Action</th>
</tr>
</thead>
<tbody>
    <#list metaCounters as c>
    <tr>
        <td><a href="counter/${c.token}">${c.token}</a></td>
        <td>${c.counter?int}</td>
        <td><button onclick='doPut("counter/${c.token}");'>Increase</button></td>
        </tr>
        </#list>
    </tbody>
</table>
