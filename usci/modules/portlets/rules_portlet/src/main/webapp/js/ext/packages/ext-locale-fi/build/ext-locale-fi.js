/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-03-11 22:33:40 (aed16176e68b5e8aa1433452b12805c0ad913836)
*/
Ext.onReady(function(){var a=Ext.ClassManager,b=Ext.Function.bind(a.get,a);if(Ext.Updater){Ext.Updater.defaults.indicatorText='<div class="loading-indicator">Ladataan...</div>'}if(Ext.Date){Ext.Date.monthNames=["tammikuu","helmikuu","maaliskuu","huhtikuu","toukokuu","kesäkuu","heinäkuu","elokuu","syyskuu","lokakuu","marraskuu","joulukuu"];Ext.Date.getShortMonthName=function(c){return(c+1)+"."};Ext.Date.monthNumbers={Jan:0,Feb:1,Mar:2,Apr:3,May:4,Jun:5,Jul:6,Aug:7,Sep:8,Oct:9,Nov:10,Dec:11};Ext.Date.getMonthNumber=function(c){if(c.match(/^(1?\d)\./)){return -1+RegExp.$1}else{return Ext.Date.monthNumbers[c.substring(0,1).toUpperCase()+c.substring(1,3).toLowerCase()]}};Ext.Date.dayNames=["sunnuntai","maanantai","tiistai","keskiviikko","torstai","perjantai","lauantai"];Ext.Date.getShortDayName=function(c){return Ext.Date.dayNames[c].substring(0,2)}}if(Ext.MessageBox){Ext.MessageBox.buttonText={ok:"OK",cancel:"Peruuta",yes:"Kyllä",no:"Ei"}}if(b("Ext.util.Format")){Ext.apply(Ext.util.Format,{thousandSeparator:".",decimalSeparator:",",currencySign:"\u20ac",dateFormat:"j.n.Y"})}if(b("Ext.util.Format")){Ext.util.Format.date=function(c,d){if(!c){return""}if(!(c instanceof Date)){c=new Date(Date.parse(c))}return Ext.Date.format(c,d||"j.n.Y")}}if(b("Ext.form.field.VTypes")){Ext.apply(Ext.form.field.VTypes,{emailText:'Syötä tähän kenttään sähköpostiosoite, esim. "etunimi.sukunimi@osoite.fi"',urlText:'Syötä tähän kenttään URL-osoite, esim. "http://www.osoite.fi"',alphaText:"Syötä tähän kenttään vain kirjaimia (a-z, A-Z) ja alaviivoja (_)",alphanumText:"Syötä tähän kenttään vain kirjaimia (a-z, A-Z), numeroita (0-9) ja alaviivoja (_)"})}});Ext.define("Ext.locale.fi.view.View",{override:"Ext.view.View",emptyText:""});Ext.define("Ext.locale.fi.grid.plugin.DragDrop",{override:"Ext.grid.plugin.DragDrop",dragText:"{0} rivi(ä) valittu"});Ext.define("Ext.locale.fi.TabPanelItem",{override:"Ext.TabPanelItem",closeText:"Sulje tämä välilehti"});Ext.define("Ext.locale.fi.view.AbstractView",{override:"Ext.view.AbstractView",msg:"Ladataan..."});Ext.define("Ext.locale.fi.picker.Date",{override:"Ext.picker.Date",todayText:"Tänään",minText:"Tämä päivämäärä on aikaisempi kuin ensimmäinen sallittu",maxText:"Tämä päivämäärä on myöhäisempi kuin viimeinen sallittu",disabledDaysText:"",disabledDatesText:"",monthNames:Ext.Date.monthNames,dayNames:Ext.Date.dayNames,nextText:"Seuraava kuukausi (Control+oikealle)",prevText:"Edellinen kuukausi (Control+vasemmalle)",monthYearText:"Valitse kuukausi (vaihda vuotta painamalla Control+ylös/alas)",todayTip:"{0} (välilyönti)",format:"j.n.Y",startDay:1});Ext.define("Ext.locale.fi.picker.Month",{override:"Ext.picker.Month",okText:"&#160;OK&#160;",cancelText:"Peruuta"});Ext.define("Ext.locale.fi.toolbar.Paging",{override:"Ext.PagingToolbar",beforePageText:"Sivu",afterPageText:"/ {0}",firstText:"Ensimmäinen sivu",prevText:"Edellinen sivu",nextText:"Seuraava sivu",lastText:"Viimeinen sivu",refreshText:"Päivitä",displayMsg:"Näytetään {0} - {1} / {2}",emptyMsg:"Ei tietoja"});Ext.define("Ext.locale.fi.form.field.Base",{override:"Ext.form.field.Base",invalidText:"Tämän kentän arvo ei kelpaa"});Ext.define("Ext.locale.fi.form.field.Text",{override:"Ext.form.field.Text",minLengthText:"Tämän kentän minimipituus on {0}",maxLengthText:"Tämän kentän maksimipituus on {0}",blankText:"Tämä kenttä on pakollinen",regexText:"",emptyText:null});Ext.define("Ext.locale.fi.form.field.Number",{override:"Ext.form.field.Number",minText:"Tämän kentän pienin sallittu arvo on {0}",maxText:"Tämän kentän suurin sallittu arvo on {0}",nanText:"{0} ei ole numero"});Ext.define("Ext.locale.fi.form.field.Date",{override:"Ext.form.field.Date",disabledDaysText:"Ei käytössä",disabledDatesText:"Ei käytössä",minText:"Tämän kentän päivämäärän tulee olla {0} jälkeen",maxText:"Tämän kentän päivämäärän tulee olla ennen {0}",invalidText:"Päivämäärä {0} ei ole oikeassa muodossa - kirjoita päivämäärä muodossa {1}",format:"j.n.Y",altFormats:"j.n.|d.m.|mdy|mdY|d|Y-m-d|Y/m/d"});Ext.define("Ext.locale.fi.form.field.ComboBox",{override:"Ext.form.field.ComboBox",valueNotFoundText:undefined},function(){Ext.apply(Ext.form.field.ComboBox.prototype.defaultListConfig,{loadingText:"Ladataan..."})});Ext.define("Ext.locale.fi.form.field.HtmlEditor",{override:"Ext.form.field.HtmlEditor",createLinkText:"Anna linkin URL-osoite:"},function(){Ext.apply(Ext.form.field.HtmlEditor.prototype,{buttonTips:{bold:{title:"Lihavoi (Ctrl+B)",text:"Lihavoi valittu teksti.",cls:Ext.baseCSSPrefix+"html-editor-tip"},italic:{title:"Kursivoi (Ctrl+I)",text:"Kursivoi valittu teksti.",cls:Ext.baseCSSPrefix+"html-editor-tip"},underline:{title:"Alleviivaa (Ctrl+U)",text:"Alleviivaa valittu teksti.",cls:Ext.baseCSSPrefix+"html-editor-tip"},increasefontsize:{title:"Suurenna tekstiä",text:"Kasvata tekstin kirjasinkokoa.",cls:Ext.baseCSSPrefix+"html-editor-tip"},decreasefontsize:{title:"Pienennä tekstiä",text:"Pienennä tekstin kirjasinkokoa.",cls:Ext.baseCSSPrefix+"html-editor-tip"},backcolor:{title:"Tekstin korostusväri",text:"Vaihda valitun tekstin taustaväriä.",cls:Ext.baseCSSPrefix+"html-editor-tip"},forecolor:{title:"Tekstin väri",text:"Vaihda valitun tekstin väriä.",cls:Ext.baseCSSPrefix+"html-editor-tip"},justifyleft:{title:"Tasaa vasemmalle",text:"Tasaa teksti vasempaan reunaan.",cls:Ext.baseCSSPrefix+"html-editor-tip"},justifycenter:{title:"Keskitä",text:"Keskitä teksti.",cls:Ext.baseCSSPrefix+"html-editor-tip"},justifyright:{title:"Tasaa oikealle",text:"Tasaa teksti oikeaan reunaan.",cls:Ext.baseCSSPrefix+"html-editor-tip"},insertunorderedlist:{title:"Luettelo",text:"Luo luettelo.",cls:Ext.baseCSSPrefix+"html-editor-tip"},insertorderedlist:{title:"Numeroitu luettelo",text:"Luo numeroitu luettelo.",cls:Ext.baseCSSPrefix+"html-editor-tip"},createlink:{title:"Linkki",text:"Tee valitusta tekstistä hyperlinkki.",cls:Ext.baseCSSPrefix+"html-editor-tip"},sourceedit:{title:"Lähdekoodin muokkaus",text:"Vaihda lähdekoodin muokkausnäkymään.",cls:Ext.baseCSSPrefix+"html-editor-tip"}}})});Ext.define("Ext.locale.fi.form.Basic",{override:"Ext.form.Basic",waitTitle:"Odota..."});Ext.define("Ext.locale.fi.grid.header.Container",{override:"Ext.grid.header.Container",sortAscText:"Järjestä A-Ö",sortDescText:"Järjestä Ö-A",lockText:"Lukitse sarake",unlockText:"Vapauta sarakkeen lukitus",columnsText:"Sarakkeet"});Ext.define("Ext.locale.fi.grid.GroupingFeature",{override:"Ext.grid.GroupingFeature",emptyGroupText:"(ei mitään)",groupByText:"Ryhmittele tämän kentän mukaan",showGroupsText:"Näytä ryhmissä"});Ext.define("Ext.locale.fi.grid.PropertyColumnModel",{override:"Ext.grid.PropertyColumnModel",nameText:"Nimi",valueText:"Arvo",dateFormat:"j.m.Y"});Ext.define("Ext.locale.fi.Component",{override:"Ext.Component"});