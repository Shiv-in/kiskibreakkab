package com.example.kiskibreakkab.domain.usecase

import com.example.kiskibreakkab.domain.model.TimetableSlot
import com.example.kiskibreakkab.domain.repository.SquadRepository
import javax.inject.Inject

class GetCommonFreeSlotsUseCase @Inject constructor(
    private val repository: SquadRepository
) {
    suspend operator fun invoke(memberIds: List<String>): Result<List<TimetableSlot>> {
        // Logic to ensure we don't calculate for empty groups
        if (memberIds.size < 2) return Result.failure(Exception("At least 2 members required for synchronization"))
        
        return repository.getCommonFreeSlots(memberIds)
    }
}
