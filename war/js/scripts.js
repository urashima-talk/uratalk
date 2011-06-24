
$.replacerLabel = function(emailText, size){
    var labels = null;
    for(var i = 0; i < size; i++){
        if(i == 0){
            labels = '<th>'
            + '$' + String.fromCharCode(65 + i)
            + ' ('
            + emailText
            + ')'
            + '<\/th>';
        } else {
            labels += '<th>'
            + '$' + String.fromCharCode(65 + i)
            +'<\/th>';
        }
    }
    return labels;
}

$.replacerValues = function(data, size){
    var values = null;
    var dataSize = data.length;
    if(dataSize == 0){
        return null;
    }
    
    for(var i = 0; i < size; i++){
        if(i == 0){
            values = '<td>' + $.escapeHTML(data[i]) + '<\/td>';
        } else {
            if(i >= dataSize){
                values += '<td><\/td>';
            } else {
                values += '<td>' + $.escapeHTML(data[i]) + '<\/td>';
            }
        }
    }
    return values;
}

$.showFormTitle = function(formId) {
    var id = $("#"+formId).find("input[name=id]").val();
    if(id){
        $("#editTitle").show();
    } else {
        $("#addTitle").show();
    }
}

$.checkAll = function(elem, className){
    $("." + className).attr("checked", $(elem).attr("checked"));
};

$.editPointForm = function (id) {
    if($.editMode) {
        return false;
    }
    $.editMode = true;
    $.initForm("pointForm", "/my/plot/pointjson", {
        "id": id
    }, function(jsonData){
        var css = $("#pointItem_" + id).attr("class");
        var values = jsonData.values;

        $("#pointItem_" + id).hide().after($( "#pointEdit" ).tmpl( values ).attr("class", css));
        $('#pointEditSubmit').val(jsonData.submit);
        $.setFormValues( "pointEditForm", jsonData );

    });
};

$.renderGraph = function(targetId, jsonData) {
    try {
        var values = jsonData.values;
        var unitNameText = values.unitName
        if(values.unit && (values.unit.length > 0)){
            unitNameText += ' (' + values.unit + ')';
        }

        $(".plotName").text(values.name);
        if(values.unitName){
            $(".plotUnitName").text(values.unitName);
            $(".plotUnitNameText").text(unitNameText);
        }
        if(values.unit){
            $(".plotUnit").text(values.unit);
        }

        if($.isArray(values.data[0]) && (values.data[0].length > 0)) {
            $.jqplot(targetId, values.data, {
                title:'',
                legend:{
                    show: true
                },
                axes:{
                    xaxis:{
                        renderer:$.jqplot.DateAxisRenderer,
                        tickOptions:{
                            formatString:'%Y/%m/%d <br \/> %H:%M'
                        }
                    },
                    yaxis:{
                        tickOptions:{
                            formatString:'%.2f'
                        }
                    }
                },
                axesDefaults:{
                    useSeriesColor: true
                },
                series: values.series,
                grid: {
                    gridLineColor: '#f0f0f0',
                    background: '#ffffff',
                    borderColor: '#666666',
                    borderWidth: 1,
                    shadow: false,
                    renderer: $.jqplot.CanvasGridRenderer,
                    rendererOptions: {}
                }
            });
        }
    } catch (e) {
        $("#grobalError").text(e.toString()).slideDown("fast");
    }
};