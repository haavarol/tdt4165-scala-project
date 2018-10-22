import scala.collection.mutable.Queue

object TransactionStatus extends Enumeration {
    val SUCCESS, PENDING, FAILED = Value
}

class Transaction(val from: Account,
                  val to: Account,
                  val amount: Double,
                  var status: TransactionStatus.Value = TransactionStatus.PENDING,
                  val id: String = java.util.UUID.randomUUID.toString,
                  var receiptReceived: Boolean = false,
                  val allowedAttempts: Int) {
    var attemptsTried = 0
    override def run: Unit = {

        def doTransaction() = {
            var hasWithdrawn = false

            try {
                from withdraw amount
                hasWithdrawn = true
                to deposit amount
                status = TransactionStatus.SUCCESS

            } catch {
                case e: Exception => {
                    if (hasWithdrawn) {
                        from.deposit(amount) // transfer amount back
                    }
                    attemptsTried += 1
                    if(attemptsTried == allowedAttempts) {
                        status = TransactionStatus.FAILED
                    }
                }
            }
        }

        if (from.uid < to.uid) from synchronized {
            to synchronized {
                doTransaction
            }
        } 
        else to synchronized {
            from synchronized {
                doTransaction
            }
        }  
    }

    def isCompleted: Boolean = {
        this.status != TransactionStatus.PENDING
    }

    def isSuccessful: Boolean = {
        isCompleted && this.status == TransactionStatus.SUCCESS
    }

}

class TransactionQueue {

    var queue = new Queue[Transaction]()

    // Remove and return the first element from the queue
    def pop: Transaction = this.synchronized {
      queue.dequeue()
    }

    // Return whether the queue is empty
    def isEmpty: Boolean = this.synchronized {queue.isEmpty}

    // Add new element to the back of the queue
    def push(t: Transaction): Unit = this.synchronized { queue.enqueue(t) }

    // Return the first element from the queue without removing it
    def peek: Transaction = this.synchronized { queue.front}

    // Return an iterator to allow you to iterate over the queue
    def iterator: Iterator[Transaction] = this.synchronized {
      queue.iterator
    }

}