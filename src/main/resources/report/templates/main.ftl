<!DOCTYPE html>
<html lang="en">
    <head>
        <#include "css/chameleon.css">
        <meta charset="UTF-8">
        <title>Chameleon Report</title>
    </head>
    <body>
        <h1>Chameleon Report</h1>
        <div>Results of the image comparison can be found below...</div>
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
                             <button class="diff-button" onclick="this.nextElementSibling.removeAttribute('hidden');">
                             (Click here to see the diff)
                             </button>
                             <div class="diff-modal-container" hidden>
                                 <div class="diff-modal">
                                    <button class="close-button" onclick="this.parentElement.parentElement.hidden = true">&times;</button>
                                    <div class="image-container">
                                       <p>Diff [ ${result.name} ] :</p>
                                       <img src="${result.diffImagePath}">
                                    </div>
                                 </div>
                             </div>
                        </#if>
                        </div>
                    </div>

                    <div class="images-container">
                        <div class="image-container">
                            <p>Reference:</p>
                            <img src="${result.refImagePath}">
                        </div>
                        <div class="image-container">
                            <p>Test:</p>
                            <img src="${result.resultImagePath}">
                        </div>
                    </div>
                </div>
            </#list>
        </div>
    </body>
</html>
