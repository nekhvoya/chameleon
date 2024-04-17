<!DOCTYPE html>
<html lang="en">
    <head>
        <#include "assets/chameleon.css">
        <meta charset="UTF-8">
        <title>Chameleon Report</title>
    </head>
    <body>
        <div class="logo">
            <img src="./logo.png" alt="Little Chameleon">
        </div>
        <div class="title">
            <h1>Chameleon Report</h1>
        </div>
        <div class="report-container">
            <#list results as result>
                <div class="result-container">
                    <div class="result-summary">
                        <div>Test   [ ${result.name} ]   --->
                        <#if result.passed>
                            <span class="passed"> PASSED </span>
                        <#else>
                            <span class="failed"> FAILED </span>
                        </#if>
                        <#if !result.passed>
                             <#if result.diffImagePath??>
                                 <button class="diff-button" onclick="this.nextElementSibling.removeAttribute('hidden');">
                                 (Click here to see the diff)
                                 </button>
                                 <@imageModal type='Diff' resultName=result.name imagePath=result.diffImagePath></@imageModal>
                            </#if>
                            <div class="errors">
                                <#list result.errors as error>
                                    <p><span class="error">ERROR:</span> ${error}</p>
                                </#list>
                            <div>
                            <div class="warnings">
                                <#list result.warnings as warning>
                                    <p><span class="warning">WARNING:</span> ${warning}</p>
                                </#list>
                            <div>
                        </#if>
                        </div>
                    </div>

                    <div class="images-container">
                        <div class="image-container">
                            <p>Reference:</p>
                            <#if result.refImagePath??>
                                <img src="${result.refImagePath}" onclick="this.nextElementSibling.removeAttribute('hidden');">
                                <@imageModal type='Reference' resultName=result.name imagePath=result.refImagePath></@imageModal>
                            </#if>
                        </div>
                        <div class="image-container">
                            <p>Test:</p>
                            <#if result.resultImagePath??>
                                <img src="${result.resultImagePath}" onclick="this.nextElementSibling.removeAttribute('hidden');">
                                <@imageModal type='Test' resultName=result.name imagePath=result.resultImagePath></@imageModal>
                            </#if>
                        </div>
                    </div>
                </div>
            </#list>
        </div>
    </body>
</html>

<#macro imageModal type resultName imagePath>
<div class="image-modal-container" hidden onclick="if(!event.target.closest('.image-modal')) event.target.hidden=true">
     <div class="image-modal">
          <button class="close-button" onclick="this.parentElement.parentElement.hidden = true">&times;</button>
          <div class="image-container">
               <p>${type} [ ${resultName} ] :</p>
               <img src="${imagePath}">
          </div>
     </div>
</div>
</#macro>