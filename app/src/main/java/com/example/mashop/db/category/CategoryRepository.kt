package com.example.mashop.db.category

import com.example.mashop.db.supabase
import io.github.jan.supabase.postgrest.from

class CategoryRepository {
    companion object {
        suspend fun getAllCategories(): List<Category> {
            return supabase.from("category").select().decodeList()
        }

//        suspend fun getCategory(name: String): Category? {
//            return supabase.from("category").select {
//                filter {
//                    Category::name eq name
//                }
//            }.decodeSingleOrNull()
//        }
    }
}