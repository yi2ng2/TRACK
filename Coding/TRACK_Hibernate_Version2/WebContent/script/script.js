function showHide(callerID, ctrlID, displayText){
	if(document.getElementById(ctrlID)){
		if(document.getElementById(ctrlID).style.display != 'none'){
			document.getElementById(ctrlID).style.display = 'none';
			document.getElementById(callerID).innerHTML = '[ + ] ' + displayText;
		}else{
			document.getElementById(ctrlID).style.display = 'block';
			document.getElementById(callerID).innerHTML = '[ - ] ' + displayText;
		}
	}
}
