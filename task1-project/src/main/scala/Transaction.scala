import exceptions._

import scala.collection.mutable.Queue

object TransactionStatus extends Enumeration {
  val SUCCESS, PENDING, FAILED = Value
}

class TransactionQueue {

    var queue = new Queue[Transaction]()

    // Remove and return the first element from the queue
    def pop: Transaction = queue.dequeue()

    // Return whether the queue is empty
    def isEmpty: Boolean = if(queue.isEmpty) true else false

    // Add new element to the back of the queue
    def push(t: Transaction): Unit = queue.enqueue(t)

    // Return the first element from the queue without removing it
    def peek: Transaction = queue.front

    // Return an iterator to allow you to iterate over the queue
    def iterator: Iterator[Transaction] = this.synchronized {
      queue.iterator
    }

    // TODO: Find out if any of the above methods need to be synchronized
}

class Transaction(val transactionsQueue: TransactionQueue,
                  val processedTransactions: TransactionQueue,
                  val from: Account,
                  val to: Account,
                  val amount: Double,
                  val allowedAttemps: Int) extends Runnable {
  var status: TransactionStatus.Value = TransactionStatus.PENDING
  override def run: Unit = {
    def doTransaction() = {
      from withdraw amount
      to deposit amount
    }
    if (from.uid < to.uid) from synchronized {
      to synchronized {
        doTransaction
      }
    } else to synchronized {
      from synchronized {
        doTransaction
      }
    }
    // TODO: Extend this method to satisfy requirements.
  }
}
