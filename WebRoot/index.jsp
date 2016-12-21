<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="sx" uri="/struts-dojo-tags" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    
    <title>KEG-EntityLinking</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="Entity linking system producted by KEG">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->
		<script type="text/javascript">
		var XHR = false;
		function CreateXHR(){
			try{
				XHR = new ActiveXObject("msxml2.XMLHTTP");
			}catch(e1){
				try{
					XHR = new ActiveObject("microsoft.XMLHTTP");
				}catch(e2){
					try{
						XHR = new XMLHttpRequest();
					}catch(e3){
						XHR = false;
					}
				}
			}
		}
		
		function sendRequest(){
			CreateXHR();
			if(XHR){
				var text = document.getElementById("text").value;
				var uri = "http://10.1.1.68:8080/EntityLinkingWeb/linkingSubmit.action?text=" + text;
				console.log("text:" + text);
				console.log("uri:" + uri);
				XHR.open("GET", uri, true);
				XHR.onreadystatechange = resultHander;
				XHR.send(null);
			}
		}
		function resultHander(){
			if(XHR.readyState == 4 && XHR.status == 200){
				console.log(XHR.response);
            	console.log(XHR.responseText);
				var userObj = JSON.parse(XHR.responseText);
				var resultObj = userObj.ResultList;
				var tbl=$("<table/>").attr("id","mytable");
				$("#jsonShow").append(tbl);
				$("#mytable").append("<td>labelr</td<tr><td>start</td><td>end</td>><td>url</td></tr>"); 
				for(var i=0;i<resultObj.length;i++)
				{
				    var tr="<tr>";
				    var td1="<td>"+resultObj[i]["label"]+"</td>";
				    var td2="<td>"+resultObj[i]["start_index"]+"</td>";
				    var td3="<td>"+resultObj[i]["end_index"]+"</td>";
				    var td4="<td>"+resultObj[i]["url"]+"</td></tr>";
				    
				   $("#mytable").append(tr+td1+td2+td3+td4); 
				  
				}
				document.getElementById('jsonShow').innerHTML = userObj.ResultList;
			}
		}
		</script>
  </head>
  
  <body>
  	<div class="container-fluid">
	<div class="row-fluid">
		<div class="span12">
			<h2 class="text-left">
				<em>KEG Entity Linking System</em>
			</h2>
			<div class="span6">
				<form>
					<fieldset>
						<p>
							<br /><input type="text" id="text" style="width: 400px; height: 104px"/>
						</p>
						<p>
							<button class="btn" type="submit" onclick="sendRequest();">提交</button>
						</p>
					</fieldset>
				</form>
			</div>
			<div class="span6">
				<h3>
					<em>结果</em>
				</h3>
				<p id="jsonShow">
				</p>
			</div>
		</div>
	</div>
	</div>
	<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="http://cdn.bootcss.com/jquery/1.11.1/jquery.min.js"></script>
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="js/bootstrap.min.js"></script>
  </body>
</html>
