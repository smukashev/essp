package kz.bsbnb.usci.tools
/**
 * Created by emles on 07.09.17
 */

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

def pledge47 = {
    """
                <pledge>
                    <pledge_type>47</pledge_type>
                </pledge>"""
}

def pledge = { pledgeType, contractNo, value ->
    """
                <pledge>
                    <pledge_type>${pledgeType}</pledge_type>
                    <contract>
                        <no>${contractNo}</no>
                    </contract>
                    <value>${value}</value>
                </pledge>"""
}

def getPledges = { List pledges ->
    StringBuffer buffer = new StringBuffer("")
    pledges.each { Map pledgeMap ->
        if (pledgeMap.pledgeType == 47) buffer.append(pledge47())
        else buffer.append(pledge(pledgeMap.pledgeType, pledgeMap.contractNo, pledgeMap.value))
    }
    buffer.toString()
}

getBatchXml = { contractNo, contractDate, reportDate, action, allPledges ->

    """<?xml version="1.0" encoding="UTF-8"?>
<batch>
    <info>
        <creditor>
            <docs>
                <doc doc_type="15">
                    <no>KINCKZKA</no>
                </doc>
            </docs>
        </creditor>
        <account_date>2017-07-20</account_date>
        <report_date>${reportDate}</report_date>
        <actual_credit_count>17694</actual_credit_count>
    </info>
    <packages>
        <package operation_type="${action}" no="1247">
            <primary_contract>
                <no>test-7</no>
                <date>2017-03-20</date>
            </primary_contract>
            <credit credit_type="12">
                <currency>KZT</currency>
                <interest_rate_yearly>2</interest_rate_yearly>
                <contract_maturity_date>2017-12-31</contract_maturity_date>
                <actual_issue_date>2017-03-18</actual_issue_date>
                <amount>135839</amount>
                <has_currency_earn>0</has_currency_earn>
                <creditor_branch>
                    <docs>
                        <doc doc_type="07">
                            <no>111241004149</no>
                        </doc>
                    </docs>
                </creditor_branch>
                <portfolio>
                    <portfolio_msfo nullify='true'></portfolio_msfo>
                </portfolio>
            </credit>
            <pledges>${getPledges(allPledges)}
            </pledges>
        </package>
    </packages>
</batch>
"""

}

def zipBatch = { zipFileName, batchFileName, String batchXml ->

    byte[] batchXmlBytes = batchXml.getBytes("UTF-8")

    ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(zipFileName))
    zipFile.putNextEntry(new ZipEntry(batchFileName))
    zipFile.write(batchXmlBytes)
    zipFile.closeEntry()
    zipFile.flush()
    zipFile.close()

}

def zipPath = "/opt/projects/info/batches/in/"

def genBatch = { contractNo, contractDate, caseFraze, reportDate, action, pledgesList ->

    def year = reportDate.split("-")[0]
    def month = reportDate.split("-")[1]

    def zipFileName = year + caseFraze + ".ZIP"
    def batchFileName = "cred_reg_file_${year}.${month}.xml"

    def batchXml = getBatchXml(contractNo, contractDate, reportDate, action, pledgesList) as String

    zipBatch(zipPath + zipFileName, batchFileName, batchXml)

}


def batchsInfo = [

        [
                contractNo  : "test-8",
                contractDate: "2017-03-20",
                caseFraze   : "-CASE-1",
                reportDate  : "2017-07-01",
                action      : "insert",
                pledgesList : [
                        [pledgeType: 22, contractNo: "pledge-1", value: "444333"],
                        /*[pledgeType: 10, contractNo: "pledge-2", value: "333222"],
                        [pledgeType: 10, contractNo: "pledge-3", value: "222111"],*/
                        /*[pledgeType: 47],*/
                ]
        ],

        [
                contractNo  : "test-7",
                contractDate: "2017-03-20",
                caseFraze   : "-CASE-2",
                reportDate  : "2017-08-01",
                action      : "update",
                pledgesList : [
                        /*[pledgeType: 22, contractNo: "pledge-1", value: "444333"],*/
                        [pledgeType: 10, contractNo: "pledge-2", value: "333222"],
                        [pledgeType: 10, contractNo: "pledge-3", value: "222111"],
                        /*[pledgeType: 47],*/
                ]
        ],

        [
                contractNo  : "test-7",
                contractDate: "2017-03-20",
                caseFraze   : "-CASE-3",
                reportDate  : "2017-09-01",
                action      : "update",
                pledgesList : [
                        [pledgeType: 22, contractNo: "pledge-1", value: "444333"],
                        /*[pledgeType: 10, contractNo: "pledge-2", value: "333222"],*/
                        [pledgeType: 10, contractNo: "pledge-3", value: "222111"],
                        /*[pledgeType: 47],*/
                ]
        ],

        [
                contractNo  : "test-7",
                contractDate: "2017-03-20",
                caseFraze   : "-CASE-4",
                reportDate  : "2017-10-01",
                action      : "update",
                pledgesList : [
                        [pledgeType: 22, contractNo: "pledge-1", value: "444333"],
                        /*[pledgeType: 10, contractNo: "pledge-2", value: "333222"],*/
                        [pledgeType: 10, contractNo: "pledge-3", value: "222111"],
                        /*[pledgeType: 47],*/
                ]
        ]

]


batchsInfo.each { batchInfo ->
    genBatch(
            batchInfo.contractNo,
            batchInfo.contractDate,
            batchInfo.caseFraze,
            batchInfo.reportDate,
            batchInfo.action,
            batchInfo.pledgesList
    )
}



