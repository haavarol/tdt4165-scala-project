import exceptions._

import scala.collection.mutable

object TransactionStatus extends Enumeration {
  val SUCCESS, PENDING, FAILED = Value
}

class TransactionQueue {

    var queue = new mutable.Queue[Transaction]()

    // Remove and return the first element from the queue
    def pop: Transaction = queue.dequeue()

    // Return whether the queue is empty
    def isEmpty: Boolean = if(queue.isEmpty) true else false

    // Add new element to the back of the queue
    def push(t: Transaction): Unit = queue.enqueue(t)

    // Return the first element from the queue without removing it
    def peek: Transaction = queue.front

    // Return an iterator to allow you to iterate over the queue
    def iterator: Iterator[Transaction] = {

      // Usikker på hvilken iterator som bør returneres
      // Den første fører til stackoverflow, den andre til en uendelig loop

      /*
      return new Iterator[Transaction] {

        override def hasNext: Boolean = {
          if (isEmpty) false else true
        }

        override def next: Transaction = pop
      }
      */

      return queue.iterator
    }
}

class Transaction(val transactionsQueue: TransactionQueue,
                  val processedTransactions: TransactionQueue,
                  val from: Account,
                  val to: Account,
                  val amount: Double,
                  val allowedAttempts: Int) extends Runnable {

  var status: TransactionStatus.Value = TransactionStatus.PENDING
  var attemptsLeft = allowedAttempts

  override def run: Unit = {

    def doTransaction() = {
        if(from.getBalanceAmount >= amount) {
          from.withdraw(amount)
          to.deposit(amount)
          status = TransactionStatus.SUCCESS
        } else {
          status = TransactionStatus.FAILED
          attemptsLeft -= 1
          throw new NoSufficientFundsException("Sender do not have balance")
        }
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

    // Extend this method to satisfy requirements.
    if (status == TransactionStatus.FAILED && attemptsLeft > 0) doTransaction()
  }
}
