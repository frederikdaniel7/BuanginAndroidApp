package com.uiprojects.buanginapp.model

data class User(
    var name: String?,
    var totalRevenue: Double?,
    var totalWeight: Double?,
    var thisMonthEarning: Double?,
    var userId : String?
    ){
    constructor() : this("",0.0,0.0,0.0,"")
}
