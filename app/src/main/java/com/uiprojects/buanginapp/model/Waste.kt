package com.uiprojects.buanginapp.model

import com.google.firebase.Timestamp

data class Waste(
    var name: String,
    var categoryId: String,
    var userId: String,
    var userName: String,
    var wasteCode: String,
    var revenue: Double?,
    var weight: Double?,
    var createdAt: Timestamp?,
    var isClaimed: Boolean,
    var isTrade: Boolean,
    var tradeDesc: String?
){
    constructor():this("","","","","",0.0,null, null,false,false,null)

}
