// showStyle is block or inline
function toggleExpandedView(id, showStyle){
    
    expandedId = id+"_expanded";
    compactedId = id+"_compacted";
    buttonId = id+"_toggleButton";
    
    expandedShowing = $("#"+expandedId).css('display') == showStyle;
    
    if(expandedShowing){
        $("#"+expandedId).css('display', 'none');
        $("#"+compactedId).css('display', showStyle);
     //   $("#"+buttonId).removeClass("ui-icon-minusthick");
     //   $("#"+buttonId).addClass("ui-icon-plusthick");

    }
    else {
        $("#"+expandedId).css('display', showStyle);
        $("#"+compactedId).css('display', 'none');
       // $("#"+buttonId).removeClass("ui-icon-plusthick");
       // $("#"+buttonId).addClass("ui-icon-minusthick");
    }
}


function hover(item){
    $(item).addClass("ui-state-hover");
}
function unhover(item){
    $(item).removeClass("ui-state-hover");
}

//$('#"+this._id.toString+"_expanded').css('display','inline'); $('#"+this._id.toString+"').css('display','none')"