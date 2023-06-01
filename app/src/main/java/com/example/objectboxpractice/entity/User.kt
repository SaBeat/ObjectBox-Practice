package com.example.objectboxpractice.entity

import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class User(
    @Id
    var id: Long = 0,
    var name: String? = ""
) : Parcelable
