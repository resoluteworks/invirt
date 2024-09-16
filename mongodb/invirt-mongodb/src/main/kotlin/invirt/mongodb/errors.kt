package invirt.mongodb

import com.mongodb.ErrorCategory
import com.mongodb.MongoCommandException
import com.mongodb.MongoException
import com.mongodb.MongoWriteException

private const val ERROR_CODE_DUPLICATE_KEY = 11000

fun MongoException.isDuplicateError(): Boolean = (this is MongoCommandException && this.errorCode == ERROR_CODE_DUPLICATE_KEY) ||
    (this is MongoWriteException && this.error.category == ErrorCategory.DUPLICATE_KEY)
