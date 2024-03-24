<!DOCTYPE html>
<html lang="en">
    <head>
        <#include "css/report.css">
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
                        <div>Test   [ ${result.name} ]   --->   ${result.passed?then('PASSED', 'FAILED')}
                        <#if !result.passed>
                             <button class="diff-button">(Click here to see the diff)</button>
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
