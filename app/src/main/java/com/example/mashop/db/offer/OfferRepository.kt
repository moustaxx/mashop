package com.example.mashop.db.offer

import com.example.mashop.db.user.User
import com.example.mashop.db.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.TextSearchType
import io.github.jan.supabase.postgrest.result.PostgrestResult

class OfferRepository {
    companion object {
        suspend fun getOffer(id: Long): OfferWithSeller? {
            return supabase.from("offer").select(Columns.list(
                "id", "created_at", "title", "content", "price", "seller_id", "category_id",
                "is_available", "image_url", "user(*)"
            )) {
                filter {
                    Offer::id eq id
                    User::id eq Offer::sellerId
                }
            }.decodeSingleOrNull()
        }

        suspend fun getLastAddedOffers(limit: Long = 10): List<Offer> {
            return supabase.from("offer").select {
                limit(limit)
                order("created_at", Order.DESCENDING)
            }.decodeList()
        }

        suspend fun getCategoryOffers(id: Long, limit: Long = 50): List<Offer> {
            return supabase.from("offer").select {
                limit(limit)
                order("created_at", Order.DESCENDING)
                filter {
                    Offer::categoryId eq id
                }
            }.decodeList()
        }

        suspend fun searchForOffers(phrase: String, limit: Long = 50): List<Offer> {
            return supabase.from("offer").select {
                limit(limit)
                filter {
                    textSearch("title", phrase, TextSearchType.WEBSEARCH)
                }
            }.decodeList()
        }

        suspend fun addOffer(offer: OfferCreateInput): PostgrestResult {
            return supabase.from("offer").insert(offer)
        }

        suspend fun getOffersFromList(idList: List<Long>): List<Offer> {
            return supabase.from("offer").select {
                filter {
                    isIn("id", idList)
                }
            }.decodeList()
        }
    }
}